import { Command, commands as com } from '../../command';
import { Variable } from '../../context';
import { ResultType } from '../../interfaces';
import { Query } from '../../query';
import { CommandNode } from './CommandNode';
import { command, CommandDefinition } from './commands';
import { DocItem } from './DocItem';
import { QueryNode } from './QueryNode';
import { fromConstructor, param as p } from './Signature';

export const builtinCommands: { [name: string]: CommandDefinition & DocItem } = {
    ADD_TO: {
        params: [p.any, p.any],
        returnsTypeFromLastChild: true,
        factory: fromAssignmentConstructor(com.AddToCommand),
        doc: com.AddToCommand.doc,
    },
    ASSIGN: {
        params: [p.any, p.restAny],
        factory: fromConstructor(com.AssignCommand),
        doc: com.AssignCommand.doc,
    },
    CLEAR_GRAPH: {
        params: [p.any],
        returns: ResultType.Number,
        factory: fromConstructor(com.ClearGraphCommand),
        doc: com.ClearGraphCommand.doc,
    },
    COPY_IN_GRAPH: {
        params: [p.any, p.any, p.optionalBoolean],
        factory: fromConstructor(com.CopyInGraphCommand),
        doc: com.CopyInGraphCommand.doc,
    },
    DESTROY: {
        params: [p.any],
        factory: fromConstructor(com.DestroyCommand),
        doc: com.DestroyCommand.doc,
    },
    ERROR: {
        params: [p.restAny],
        factory: fromConstructor(com.ErrorCommand),
        doc: com.ErrorCommand.doc,
    },
    FOREACH: {
        params: [p.any, p.any],
        factory: fromConstructor(com.ForEachCommand),
        // assign all the existing variables and the 3 for the forEachCommand
        link: vars => ({ ...vars, $value: com.$value, $index: com.$index, $array: com.$array }),
        doc: com.ForEachCommand.doc,
    },
    FORKJOIN: {
        params: [p.restAny],
        // Clone the vars for our children. Any new variables created inside should not leak outside this command.
        link: vars => ({ ...vars }),
        factory: fromConstructor(com.CommandListParallel),
        doc: com.CommandListParallel.doc,
    },
    IF: {
        params: [p.boolean, p.any, p.optionalAny],
        factory: fromConstructor(com.IfCommand),
        doc: com.IfCommand.doc,
    },
    LOG: {
        params: [p.restAny],
        factory: fromConstructor(com.LogCommand),
        doc: com.LogCommand.doc,
    },
    MAP: {
        params: [p.any, p.any],
        factory: fromConstructor(com.MapCommand),
        // assign all the existing variables and the 3 for the mapCommand
        link: vars => ({ ...vars, $value: com.$value, $index: com.$index, $array: com.$array }),
        doc: com.MapCommand.doc,
    },
    NEW: {
        params: [p.any, p.any],
        factory: fromConstructor(com.NewCommand),
        doc: com.NewCommand.doc,
    },
    NEW_GRAPH: {
        params: [p.any],
        factory: fromConstructor(com.NewGraphCommand),
        doc: com.NewGraphCommand.doc,
    },
    NEW_TRANSIENT_GRAPH: {
        params: [],
        factory: fromConstructor(com.NewTransientGraphCommand),
        doc: com.NewTransientGraphCommand.doc,
    },
    QUERY_TO_COMMAND: {
        params: [p.any],
        returnsTypeFromLastChild: true,
        // Is a special case, all arguments are already converted to Commands, so we can simply return it.
        factory: ([p1]) => p1,
        doc: undefined, // Can and will never be called by consumers of library, is only needed by JISON to convert queries to commands in grammar.
    },
    REMOVE_FROM: {
        params: [p.any, p.any],
        returnsTypeFromLastChild: true,
        factory: fromAssignmentConstructor(com.RemoveFromCommand),
        doc: com.RemoveFromCommand.doc,
    },
    RETURN: {
        params: [p.optionalAny],
        factory: fromConstructor(com.ReturnCommand),
        doc: com.ReturnCommand.doc,
    },
    SERIAL: {
        params: [p.restAny],
        returnsTypeFromLastChild: true,
        // Clone the vars for our children. Any new variables created inside should not leak outside this command.
        link: vars => ({ ...vars }),
        factory: fromConstructor(com.CommandListSerial),
        doc: com.CommandListSerial.doc,
    },
    WARNING: {
        params: [p.restAny],
        factory: fromConstructor(com.WarningCommand),
        doc: com.WarningCommand.doc,
    },
};

export const draftingCommands: { [name: string]: CommandDefinition & DocItem } = {
    NEW_DRAFT: {
        params: [p.any, p.any],
        returns: ResultType.Iri,
        factory: fromConstructor(com.NewDraftCommand),
        doc: com.NewDraftCommand.doc,
    },
    NEW_DRAFT_GRAPH: {
        params: [],
        returns: ResultType.Iri,
        factory: fromConstructor(com.NewDraftGraphCommand),
        doc: com.NewDraftGraphCommand.doc,
    },
};

export function declareVariables(names: string[], initValues?: CommandNode[]): CommandNode {
    const variables = names.map(n => new Variable(n));
    const def: CommandDefinition = {
        params: [p.restAny],
        factory: (params: Command[]) => new com.DeclareVariableCommand(variables, initValues && params),
        link: vars => {
            // Create a copy for our child expressions.
            const forChildren = { ...vars };
            // Add the variables to the original map for following commands in flow.
            for (const v of variables) { vars[v.name] = v; }
            // Return the copy that we created for our children.
            return forChildren;
        },
    };
    return command(def, initValues || []);
}

export function assign(lhs: QueryNode[], rhs: CommandNode[]): CommandNode {
    const def: CommandDefinition = {
        params: [p.restAny],
        factory: () => new com.SetToCommand(lhs.map(e => e.query), rhs.map(e => e.impl)),
    };
    return command(def, [...lhs, ...rhs]);
}

/**
 * Creates a new CommandDefinition that can be used anywhere a command is acceptable. The internal logic
 * can have access to variables from a current context, therefore the returned node needs to be compiled like
 * any other node before the CommandDefinition (lambda) can be used.
 *
 * @param logic the internal logic of the lambda command
 * @param params the parameters that the command accepts
 */
export function commandClosure(logic: CommandNode, params: string[]): { node: CommandNode, lambda: CommandDefinition } {
    const paramVariables = params.map(n => new Variable(n));

    // lambda is the reusable CommandDefinition.
    const lambda: CommandDefinition & { lambdaImpl?: com.Lambda } = {
        params: params.map(() => p.any),
        get returns() { return logic.type; },
        factory: paramValues => {
            // istanbul ignore if: Should not be possible
            if (!lambda.lambdaImpl) {
                throw new Error('The returned node from commandClosure has to be linked before finalizing the returned lambda');
            }
            return lambda.lambdaImpl.call(paramValues);
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
            lambda.lambdaImpl = new com.Lambda(logic.impl, paramVariables);
            // Do nothing here, we only want to remember the new lambda.
            return com.empty();
        },
    }, [logic]);

    return { node, lambda };
}

function fromAssignmentConstructor(constructor: new (lhs: Query, rhs: Command) => Command): (params: Command[]) => Command {
    return ([lhs, rhs]) => {
        if (!(lhs instanceof com.QueryCommand)) {
            throw new Error('Left hand side is not assignable.');
        }
        return new constructor(lhs.query, com.queryCmd(rhs));
    };
}

/**
 * Registers an ODT command as global "builtin" command.
 *
 * @param name the name to use in ODT scripts for this command
 * @param cmd the command definition
 */
export function registerBuiltinCommand(name: string, cmd: CommandDefinition) {
    if (!builtinCommands[name]) {
        builtinCommands[name] = cmd as CommandDefinition & DocItem;
    } else if (builtinCommands[name] !== cmd) {
        throw new Error(`another command with name ${name} was already registered`);
    }
}
