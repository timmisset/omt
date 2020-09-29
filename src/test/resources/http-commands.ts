import {HttpCall, HttpClient, HttpMethod, HttpRequest, HttpResponse, queryCmd} from './command/impls';
import {Commands, param} from './dsl';
import {DocItem} from './dsl/ast/DocItem';
import {ResultType} from './interfaces';
import {Q} from './query';

export {HttpClient, HttpMethod, HttpRequest, HttpResponse};

export function createHttpCommands(http: HttpClient): Commands & { [command: string]: DocItem } {
    return {
        HTTP_GET: {
            params: [param.string, param.optionalAny, param.optionalBoolean],
            returns: ResultType.JSON,
            factory: ([url, queryParams, throwsOnError]) => new HttpCall(http, constant('GET'), url, undefined, throwsOnError, queryParams),
            doc: HttpCall.docGet,
        },
        HTTP_POST: {
            params: [param.string, {type: ResultType.JSON}, param.optionalBoolean, param.optionalAny],
            returns: ResultType.JSON,
            factory: ([url, body, throwsOnError, queryParams]) => new HttpCall(http, constant('POST'), url, body, throwsOnError, queryParams),
            doc: HttpCall.docPost,
        },
        HTTP_PUT: {
            params: [param.string, {type: ResultType.JSON}, param.optionalBoolean, param.optionalAny],
            returns: ResultType.JSON,
            factory: ([url, body, throwsOnError, queryParams]) => new HttpCall(http, constant('PUT'), url, body, throwsOnError, queryParams),
            doc: HttpCall.docPut,
        },
    };
}

function constant(s: string) {
    return queryCmd(Q.constant(s));
}
