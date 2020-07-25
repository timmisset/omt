import { commands as com } from '../../command';
import { Variable } from '../../context';
import { ResourceList, ResultType } from '../../interfaces';
import { operators as ops, ParameterizedQuery, Q, Query } from '../../query';
import { constants, PrimitiveType, toResultType } from '../../util';
import { CommandNode } from './CommandNode';
import { command } from './commands';
import { DocItem } from './DocItem';
import { operator, OperatorDefinition } from './operators';
import { QueryNode } from './QueryNode';
import { fromConstructor, param as p, ParamDef } from './Signature';

const IGNORE_CASE_OPTION = 'ignoreCase';
const ignoreCaseParam: ParamDef = { ...p.optionalBoolean, forbiddenOption: IGNORE_CASE_OPTION };

function ignoreCaseOption(options: string[]) {
    return Q.constant(options.includes(IGNORE_CASE_OPTION));
}

// BUILTIN OPERATORS
export const builtinOperators: { [name: string]: OperatorDefinition & DocItem } = {
    AND: {
        params: [p.boolean, p.restBoolean],
        returns: ResultType.Boolean,
        flatten: true,
        factory: fromConstructor(ops.AndOperator),
        doc: ops.AndOperator.doc,
    },
    BLANK_NODE: {
        factory: () => new ops.FromOperator(constants.blankNode.marker),
        doc: constants.blankNode.markerDoc,
    },
    CALL: {
        params: [p.any],
        factory: fromConstructor(ops.CallOperator),
        doc: ops.CallOperator.doc,
    },
    CAST: {
        params: [p.any],
        returns: ResultType.Any,
        factory: fromConstructor(ops.CastOperator),
        doc: ops.CastOperator.doc,
    },
    CATCH: {
        params: [p.any],
        factory: fromConstructor(ops.CatchOperator),
        doc: ops.CatchOperator.doc,
    },
    CEIL: {
        returns: ResultType.Number,
        factory: fromConstructor(ops.CeilOperator),
        doc: ops.CeilOperator.doc,
    },
    CONCAT: {
        params: [p.any, p.restAny],
        returns: ResultType.String,
        factory: fromConstructor(ops.ConcatOperator),
        doc: ops.ConcatOperator.doc,
    },
    CONTAINS: {
        params: [p.string, ignoreCaseParam],
        returns: ResultType.Boolean,
        flags: [
            [IGNORE_CASE_OPTION],
        ],
        factory: ([needle, ignoreCase], opts) => new ops.ContainsOperator(needle, ignoreCase || ignoreCaseOption(opts)),
        doc: ops.ContainsOperator.doc,
    },
    COUNT: {
        params: [p.optionalBoolean],
        returns: ResultType.Number,
        factory: fromConstructor(ops.CountOperator),
        doc: ops.CountOperator.doc,
    },
    CURRENT_DATE: {
        returns: ResultType.DateLike,
        factory: fromConstructor(ops.CurrentDateOperator),
        doc: ops.CurrentDateOperator.doc,
    },
    CURRENT_DATETIME: {
        params: [p.optionalString],
        returns: ResultType.DateLike,
        factory: fromConstructor(ops.CurrentDateTimeOperator),
        doc: ops.CurrentDateTimeOperator.doc,
    },
    DISTINCT: {
        params: [p.optionalAny, ignoreCaseParam],
        flags: [
            [IGNORE_CASE_OPTION],
        ],
        factory: ([mapper, ignoreCase], opts) => new ops.DistinctOperator(mapper, ignoreCase || ignoreCaseOption(opts)),
        doc: ops.DistinctOperator.doc,
    },
    DIVIDE_BY: {
        params: [p.any, p.optionalAny],
        returns: ResultType.Number,
        factory: fromConstructor(ops.DivideByOperator),
        doc: ops.DivideByOperator.doc,
    },
    DURATION: {
        params: [p.number, p.string],
        returns: ResultType.Duration,
        factory: fromConstructor(ops.DurationOperator),
        doc: ops.DurationOperator.doc,
    },
    ELEMENTS: {
        returns: ResultType.Iri,
        factory: fromConstructor(ops.ElementsOperator),
        doc: ops.ElementsOperator.doc,
    },
    EMPTY: {
        returns: ResultType.Boolean,
        factory: fromConstructor(ops.EmptyOperator),
        doc: ops.EmptyOperator.doc,
    },
    EQUALS: {
        params: [p.any, p.optionalAny],
        returns: ResultType.Boolean,
        flags: [
            [IGNORE_CASE_OPTION],
        ],
        factory: ([left, right], opts) => new (opts.includes(IGNORE_CASE_OPTION) ? ops.EqualsIgnoreCaseOperator : ops.EqualsOperator)(left, right),
        doc: ops.EqualsOperator.doc,
    },
    ERROR: {
        params: [p.optionalString],
        factory: fromConstructor(ops.ErrorOperator),
        doc: ops.ErrorOperator.doc,
    },
    EVERY: {
        params: [p.optionalBoolean],
        returns: ResultType.Boolean,
        factory: fromConstructor(ops.EveryOperator),
        doc: ops.EveryOperator.doc,
    },
    EXISTS: {
        returns: ResultType.Boolean,
        factory: fromConstructor(ops.ExistsOperator),
        doc: ops.ExistsOperator.doc,
    },
    FILTER: {
        params: [p.boolean],
        factory: fromConstructor(ops.FilterOperator),
        doc: ops.FilterOperator.doc,
    },
    FIND_SUBJECTS: {
        params: [p.any, p.optionalAny, p.optionalAny],
        factory: fromConstructor(ops.FindSubjectsOperator),
        doc: ops.FindSubjectsOperator.doc,
    },
    FIRST: {
        params: [p.optionalBoolean],
        factory: fromConstructor(ops.FirstOperator),
        doc: ops.FirstOperator.doc,
    },
    FLOOR: {
        returns: ResultType.Number,
        factory: fromConstructor(ops.FloorOperator),
        doc: ops.FloorOperator.doc,
    },
    FORMAT: {
        params: [p.string, p.restAny],
        returns: ResultType.String,
        factory: fromConstructor(ops.FormatOperator),
        doc: ops.FormatOperator.doc,
    },
    FORMAT_DATE: {
        params: [p.string],
        returns: ResultType.String,
        factory: fromConstructor(ops.DateFormatOperator),
        doc: ops.DateFormatOperator.doc,
    },
    GRAPH: {
        returns: ResultType.Iri,
        factory: fromConstructor(ops.GraphOperator),
        doc: ops.GraphOperator.doc,
    },
    GREATER_THAN: {
        params: [p.any, p.optionalAny],
        returns: ResultType.Boolean,
        factory: fromConstructor(ops.GreaterThanOperator),
        doc: ops.GreaterThanOperator.doc,
    },
    GREATER_THAN_EQUALS: {
        params: [p.any, p.optionalAny],
        returns: ResultType.Boolean,
        factory: fromConstructor(ops.GreaterThanEqualsOperator),
        doc: ops.GreaterThanEqualsOperator.doc,
    },
    HAS: {
        params: [p.any, p.optionalAny],
        returns: ResultType.Boolean,
        flags: [
            [IGNORE_CASE_OPTION],
        ],
        factory: ([left, right], opts) => new (opts.includes(IGNORE_CASE_OPTION) ? ops.HasIgnoreCaseOperator : ops.HasOperator)(left, right),
        doc: ops.HasOperator.doc,
    },
    IDENTITY: {
        factory: fromConstructor(ops.IdentityOperator),
        doc: ops.IdentityOperator.doc,
    },
    IF_EMPTY: {
        params: [p.any],
        factory: fromConstructor(ops.IfEmptyOperator),
        doc: ops.IfEmptyOperator.doc,
    },
    IN: {
        params: [p.any, p.optionalAny],
        returns: ResultType.Boolean,
        flags: [
            [IGNORE_CASE_OPTION],
        ],
        factory: ([left, right], opts) => new (opts.includes(IGNORE_CASE_OPTION) ? ops.InIgnoreCaseOperator : ops.InOperator)(left, right),
        doc: ops.InOperator.doc,
    },
    INDEX_OF: {
        params: [p.string],
        returns: ResultType.Number,
        flags: [
            [IGNORE_CASE_OPTION],
        ],
        factory: ([needle, ignoreCase], opts) => new ops.IndexOfOperator(needle, ignoreCase || ignoreCaseOption(opts)),
        doc: ops.IndexOfOperator.doc,
    },
    IRI: {
        factory: () => new ops.FromOperator(constants.iriMarker),
        doc: constants.iriMarkerDoc,
    },
    JOIN: {
        params: [p.optionalString],
        returns: ResultType.String,
        factory: fromConstructor(ops.JoinOperator),
        doc: ops.JoinOperator.doc,
    },
    JSON: {
        factory: () => new ops.FromOperator(constants.types.json),
        doc: constants.jsonDoc,
    },
    LAST: {
        params: [p.optionalBoolean],
        factory: fromConstructor(ops.LastOperator),
        doc: ops.LastOperator.doc,
    },
    LENGTH: {
        returns: ResultType.Number,
        factory: fromConstructor(ops.LengthOperator),
        doc: ops.LengthOperator.doc,
    },
    LESS_THAN: {
        params: [p.any, p.optionalAny],
        returns: ResultType.Boolean,
        factory: fromConstructor(ops.LessThanOperator),
        doc: ops.LessThanOperator.doc,
    },
    LESS_THAN_EQUALS: {
        params: [p.any, p.optionalAny],
        returns: ResultType.Boolean,
        factory: fromConstructor(ops.LessThanEqualsOperator),
        doc: ops.LessThanEqualsOperator.doc,
    },
    LOG: {
        params: [p.optionalString],
        factory: fromConstructor(ops.LogOperator),
        doc: ops.LogOperator.doc,
    },
    LOWERCASE: {
        returns: ResultType.String,
        factory: fromConstructor(ops.LowerCaseOperator),
        doc: ops.LowerCaseOperator.doc,
    },
    MAP: {
        params: [p.any],
        factory: fromConstructor(ops.MapOperator),
        doc: ops.MapOperator.doc,
    },
    MAX: {
        params: [p.optionalAny],
        factory: fromConstructor(ops.MaxOperator),
        doc: ops.MaxOperator.doc,
    },
    MERGE: {
        params: [p.any, p.restAny],
        flatten: true,
        factory: fromConstructor(ops.MergeOperator),
        doc: ops.MergeOperator.doc,
    },
    MIN: {
        params: [p.optionalAny],
        factory: fromConstructor(ops.MinOperator),
        doc: ops.MinOperator.doc,
    },
    MINUS: {
        params: [p.any, p.optionalAny],
        returns: ResultType.Number,
        factory: fromConstructor(ops.MinusOperator),
        doc: ops.MinusOperator.doc,
    },
    NOT: {
        params: [p.optionalBoolean],
        factory: fromConstructor(ops.NotOperator),
        doc: ops.NotOperator.doc,
    },
    OR: {
        params: [p.boolean, p.restBoolean],
        returns: ResultType.Boolean,
        flatten: true,
        factory: fromConstructor(ops.OrOperator),
        doc: ops.OrOperator.doc,
    },
    ORDER_BY: {
        params: [p.optionalAny, p.optionalBoolean],
        factory: fromConstructor(ops.OrderByOperator),
        doc: ops.OrderByOperator.doc,
    },
    PICK: {
        params: [p.number, p.restNumber],
        factory: fromConstructor(ops.PickOperator),
        doc: ops.PickOperator.doc,
    },
    PLUS: {
        params: [p.any, p.optionalAny],
        returns: ResultType.Number,
        factory: fromConstructor(ops.PlusOperator),
        doc: ops.PlusOperator.doc,
    },
    REPEAT: {
        params: [p.any, p.optionalNumber, p.optionalNumber],
        factory: fromConstructor(ops.RepeatOperator),
        doc: ops.RepeatOperator.doc,
    },
    REPLACE: {
        params: [p.string, p.string],
        returns: ResultType.String,
        factory: fromConstructor(ops.ReplaceOperator),
        doc: ops.ReplaceOperator.doc,
    },
    REVERSE: {
        factory: fromConstructor(ops.ReverseOperator),
        doc: ops.ReverseOperator.doc,
    },
    ROUND: {
        params: [p.optionalNumber],
        returns: ResultType.Number,
        factory: fromConstructor(ops.RoundOperator),
        doc: ops.RoundOperator.doc,
    },
    SKIP: {
        params: [p.number],
        factory: fromConstructor(ops.SkipOperator),
        doc: ops.SkipOperator.doc,
    },
    SOME: {
        params: [p.optionalBoolean],
        returns: ResultType.Boolean,
        factory: fromConstructor(ops.SomeOperator),
        doc: ops.SomeOperator.doc,
    },
    SPLIT: {
        params: [p.string],
        returns: ResultType.String,
        factory: fromConstructor(ops.SplitOperator),
        doc: ops.SplitOperator.doc,
    },
    SUBSTRING: {
        params: [p.number, p.optionalNumber],
        returns: ResultType.String,
        factory: fromConstructor(ops.SubstringOperator),
        doc: ops.SubstringOperator.doc,
    },
    SUM: {
        returns: ResultType.Number,
        factory: fromConstructor(ops.SumOperator),
        doc: ops.SumOperator.doc,
    },
    TAKE: {
        params: [p.number],
        factory: fromConstructor(ops.TakeOperator),
        doc: ops.TakeOperator.doc,
    },
    TIMES: {
        params: [p.any, p.optionalAny],
        returns: ResultType.Number,
        factory: fromConstructor(ops.TimesOperator),
        doc: ops.TimesOperator.doc,
    },
    TRAVERSE: {
        params: [p.string, p.optionalBoolean],
        factory: fromConstructor(ops.TraverseOperator),
        doc: ops.TraverseOperator.doc,
    },
    TRIM: {
        returns: ResultType.String,
        factory: fromConstructor(ops.TrimOperator),
        doc: ops.TrimOperator.doc,
    },
    TRUNC: {
        params: [p.string],
        returns: ResultType.DateLike,
        factory: fromConstructor(ops.TruncOperator),
        doc: ops.TruncOperator.doc,
    },
    TYPE: {
        returns: ResultType.Iri,
        factory: fromConstructor(ops.TypeOperator),
        doc: ops.TypeOperator.doc,
    },
    UPPERCASE: {
        returns: ResultType.String,
        factory: fromConstructor(ops.UpperCaseOperator),
        doc: ops.UpperCaseOperator.doc,
    },
    WHEN: {
        params: [p.boolean, p.any, p.optionalAny],
        returns: ResultType.Any,
        factory: fromConstructor(ops.WhenOperator),
        doc: ops.WhenOperator.doc,
    },
};

export function variable(name: string): QueryNode {
    let v: Variable;
    return operator({
        get returns() { return v ? v.type : ResultType.Any; },
        link: vars => { v = vars[name]; },
        validate: () => v === undefined && `Variable ${name} not found`,
        factory: () => new ops.VariableOperator(v),
    });
}

export function from(resources: string | ResourceList): QueryNode {
    return operator({
        factory: () => new ops.FromOperator(resources),
    });
}

export function constant(value: PrimitiveType | null, type?: string): QueryNode {
    if (value === null) {
        return from(constants.emptyList);
    }
    const op = new ops.ConstantOperator(value, type);
    return operator({
        returns: toResultType(op.rdfType),
        factory: () => op,
    });
}

/**
 * Creates a new OperatorDefinition that can be used anywhere an operator is acceptable. The internal logic
 * can have access to variables from a current context, therefore the returned node needs to be compiled like
 * any other node before the OperatorDefinition (lambda) can be used.
 *
 * @param logic the internal logic of the lambda operator
 * @param params the parameters that the command accepts
 */
export function queryClosure(logic: QueryNode, params: string[]): { node: CommandNode, lambda: OperatorDefinition } {
    const paramVariables = params.map(n => new Variable(n));

    // lambda is the reusable OperatorDefinition.
    const lambda: OperatorDefinition & { lambdaImpl?: Query | ParameterizedQuery } = {
        params: params.map(() => p.any),
        get returns() { return logic.type; },
        factory: paramValues => {
            // istanbul ignore if: Should not be possible
            if (!lambda.lambdaImpl) {
                throw new Error('The returned node from queryClosure has to be linked before finalizing the returned lambda');
            }
            const l = lambda.lambdaImpl instanceof ParameterizedQuery ? lambda.lambdaImpl.bind(paramValues) : lambda.lambdaImpl;
            return new ops.CallOperator(l);
        },
    };

    // node closes over the current environment in the link phase and creates the implementation in the finalization phase.
    const node = command({
        // The logic is added as a parameter to this operator to make sure it is involved in all parse phases. This is why the
        // logic receives the correct variables as context during the link phase.
        params: [p.any],
        link: vars => {
            // Capture the variables and add the parameters.
            const clone = { ...vars };
            for (const v of paramVariables) { clone[v.name] = v; }
            return clone;
        },
        factory: () => {
            lambda.lambdaImpl = paramVariables.length ? new ParameterizedQuery(logic.query, paramVariables) : logic.query;
            // Do nothing here, we only want to remember the new lambda.
            return com.empty();
        },
    }, [logic]);

    return { node, lambda };
}

/**
 * Registers an ODT operator as global "builtin" operator.
 *
 * @param name the name to use in ODT queries for this operator
 * @param op the operator definition
 */
export function registerBuiltinOperator(name: string, op: OperatorDefinition) {
    if (!builtinOperators[name]) {
        builtinOperators[name] = op as OperatorDefinition & DocItem;
    } else if (builtinOperators[name] !== op) {
        throw new Error(`another operator with name ${name} was already registered`);
    }
}
