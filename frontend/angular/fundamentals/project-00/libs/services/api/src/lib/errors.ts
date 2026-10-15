import type { ProblemDetail } from './contracts';

/** Any non-2xx response. `problem` is always populated (synthesized if the body is not JSON). */
export class ApiError extends Error {
  constructor(
    public readonly status: number,
    public readonly problem: ProblemDetail,
    public readonly method: string,
    public readonly url: string,
  ) {
    super(problem.detail || problem.title || `HTTP ${status} ${method} ${url}`);
    this.name = 'ApiError';
    Object.setPrototypeOf(this, new.target.prototype);
  }
}

/** HTTP 401: missing, expired or invalid token / bad credentials. */
export class UnauthorizedError extends ApiError {
  constructor(problem: ProblemDetail, method: string, url: string) {
    super(401, problem, method, url);
    this.name = 'UnauthorizedError';
  }
}

export function isApiError(error: unknown): error is ApiError {
  return error instanceof ApiError;
}

export function isUnauthorizedError(error: unknown): error is UnauthorizedError {
  return error instanceof UnauthorizedError;
}