import { proxyToBackend, readJsonBody } from "@/lib/backend-proxy";

export async function GET() {
  return proxyToBackend("");
}

export async function POST(request: Request) {
  try {
    const body = await readJsonBody(request);

    return proxyToBackend("", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(body),
    });
  } catch (error) {
    if (error instanceof Response) {
      return error;
    }

    return Response.json(
      {
        timestamp: new Date().toISOString(),
        status: 500,
        error: "Internal Server Error",
        trace: "Falha inesperada ao encaminhar a criacao do formulario.",
      },
      { status: 500 },
    );
  }
}