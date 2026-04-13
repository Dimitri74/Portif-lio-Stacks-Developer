const DEFAULT_BACKEND_BASE_URL = "http://localhost:8080/api/formularios";

function getBackendBaseUrl() {
  return (process.env.BACKEND_API_BASE_URL ?? DEFAULT_BACKEND_BASE_URL).replace(
    /\/+$/,
    "",
  );
}

function buildErrorResponse(status: number, trace: string) {
  return Response.json(
    {
      timestamp: new Date().toISOString(),
      status,
      error: status === 502 ? "Bad Gateway" : "Internal Server Error",
      trace,
    },
    { status },
  );
}

export async function proxyToBackend(
  path: string,
  init?: RequestInit,
): Promise<Response> {
  const url = `${getBackendBaseUrl()}${path}`;

  try {
    const response = await fetch(url, {
      ...init,
      cache: "no-store",
      headers: {
        ...(init?.headers ?? {}),
      },
    });

    if (response.status === 204) {
      return new Response(null, { status: 204 });
    }

    const contentType = response.headers.get("content-type") ?? "application/json";
    const payload = await response.text();

    return new Response(payload, {
      status: response.status,
      headers: {
        "content-type": contentType,
      },
    });
  } catch {
    return buildErrorResponse(
      502,
      "Nao foi possivel conectar ao backend Spring Boot. Inicie a API Java em localhost:8080.",
    );
  }
}

export async function readJsonBody(request: Request) {
  try {
    return await request.json();
  } catch {
    throw buildErrorResponse(400, "JSON invalido. Verifique os campos enviados.");
  }
}