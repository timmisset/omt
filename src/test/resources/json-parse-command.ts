import {
    Command,
    CommandDefinition,
    commands,
    Context,
    createContext,
    Query,
    resolveParallel,
    Resource,
    ResourceList,
    ResultType,
    util,
} from '@opp/odt';
import {TransactionMode} from '@opp/quadstore';
import {BaseError} from '@politie/informant';
import {Derivable} from '@politie/sherlock';
import {fromPromise} from '@politie/sherlock-utils';
import {Seq} from 'immutable';
import moment from 'moment';
import {CardinalityDescriptor, getOntologyClass, isProperty, OntologyRdfSchema, ValidationDescriptor} from '../class';
import {iris, isKnownLiteralType} from '../utils';

const {queryCmd} = commands;
const {fromIri, fromJavaScript, toIri, toJSON} = util;

export class JsonParseCommand extends Command {
    readonly type = 'jsonParse';

    readonly valueCount = 1;

    constructor(private readonly json: Command, private readonly className: Command, private readonly graphName: Command) {
        super();
    }

    prepareStatement(context: Context): Derivable<ResourceList[]> {
        return resolveParallel([this.json, this.className, this.graphName].map(c => c.internalDerivable(context)))
            .map(([json, className, graphName]) => fromPromise(importData(context, toIri(className), toIri(graphName), toJSON(json))))
            .derive(d => [d.get()]);
    }
}

export function jsonParse(json: Query | Command, className: Query | Command, graphName: Query | Command) {
    return new JsonParseCommand(queryCmd(json), queryCmd(className), queryCmd(graphName));
}

export const jsonParseCommandDefinition: CommandDefinition = {
    params: [{type: ResultType.JSON}, {type: ResultType.Iri}, {type: ResultType.Iri}],
    returns: ResultType.Any,
    factory: ([json, className, graphName]) => jsonParse(json!, className!, graphName!),
};

async function importData(ctx: Context, classIri: Resource, graphName: Resource, obj: object | object[] | string) {
    // Handle creating/cloning in a transaction
    const subCtx = createContext(ctx, ctx.dataStore.createTxn({id: '<importData>'}), ctx.closed$);
    try {
        subCtx.dataStore.addGraph(graphName, TransactionMode.Edit);
        await subCtx.dataStore.ready$.toPromise({when: v => v});

        const descriptor = {maxCardinality: Infinity, minCardinality: 1, modelType: classIri};
        // toList() to make sure all the 'lazy processing' of immutable is triggered before we kill the Transaction.
        const out = importDataRecursive(ctx, subCtx, descriptor, graphName, obj).toList();

        const result = await subCtx.dataStore.apply(1).toPromise();
        if (result.status !== 'success') {
            throw result.lastError;
        }

        return out;

    } finally {
        // Always close the Transaction!
        subCtx.dataStore.close();
    }
}

type ImportData = object | util.PrimitiveType;

function importDataRecursive(
    queryCtx: Context,
    ctx: Context,
    desc: ValidationDescriptor & CardinalityDescriptor,
    graphName: Resource,
    value: ImportData | ImportData[]): ResourceList {
    if (Array.isArray(value)) {
        return Seq(value)
            .flatMap(o => {
                if (Array.isArray(o)) {
                    throw new BaseError({value, desc}, 'cannot parse a JSON with an Array of Arrays');
                }
                return importDataRecursive(queryCtx, ctx, {...desc, maxCardinality: 1}, graphName, o);
            });
    }

    // If no modelType exists, or the modelType is a Primitive type
    // Handle the value as a Primitive
    if (!desc.modelType || isKnownLiteralType(desc.modelType)) {
        try {
            const isDate = desc.modelType === iris.types.date || desc.modelType === iris.types.dateTime;
            // Cast to any, because we want moment to throw, if it can't convert the value (if the value is a boolean)
            const jsValue = isDate ? moment(value as any) : value;
            return fromJavaScript(jsValue as any, desc.modelType);
        } catch (error) {
            throw new BaseError(error, {desc, value}, 'cannot convert JavaScript value to RDF');
        }
    }

    // It's an object
    if (typeof value === 'string') {
        try {
            return fromIri(value);
        } catch (error) {
            throw new BaseError(error, {iri: value, desc}, 'encountered an unexpected external reference');
        }
    }

    if (typeof value !== 'object' || moment.isMoment(value) || moment.isDuration(value) || moment.isDate(value)) {
        const clsName = getOntologyClass(queryCtx, desc.modelType).simpleName;
        if (isProperty(desc)) {
            const property = desc;
            throw new BaseError({value, property},
                `unexpected primitive value encountered for property '${property.name}', got: ${typeof value}, expected an instance of: ${clsName}`);
        } else {
            throw new BaseError({value, desc},
                `unexpected primitive value encountered, got: ${typeof value}, expected an instance of: ${clsName}`);
        }
    }

    // The object needs to be created..
    const cls = getOntologyClass(queryCtx, desc.modelType);
    const schema = OntologyRdfSchema.get(ctx);
    const subject = schema.createSubject(ctx, desc.modelType, graphName);
    for (const propertyName of Object.getOwnPropertyNames(value)) {
        const prop = value[propertyName];

        // Get the property descriptor
        const property = cls.properties[propertyName];
        // no corresponding property descriptor found; ignore it.
        if (!property) {
            continue;
        }

        if (!property.modelType && typeof prop === 'object') {
            throw new BaseError({
                property,
                cls
            }, `encountered an untyped property '${propertyName}' of class ${cls.simpleName}`);
        }
        if (property.maxCardinality === 1 && Array.isArray(prop)) {
            throw new BaseError({property, cls, value},
                `cannot set non-collection property '${propertyName}' with an Array value ${JSON.stringify(value)}`);
        }

        schema.setModelProperty(ctx, subject, property, importDataRecursive(queryCtx, ctx, property, graphName, prop), queryCtx);
    }
    return Seq.Indexed.of(subject);
}
