import { proxyToBackend, readJsonBody } from "@/lib/backend-proxy";

type RouteParams = {
  params: Promise<{
    id: string;
  }>;
};

export async function GET(_request: Request, { params }: RouteParams) {
  const { id } = await params;
  return proxyToBackend(`/${id}`);
}

export async function PUT(request: Request, { params }: RouteParams) {
  const { id } = await params;

  try {
    const body = await readJsonBody(request);

    return proxyToBackend(`/${id}`, {
      method: "PUT",
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
        trace: "Falha inesperada ao encaminhar a atualizacao do formulario.",
      },
      { status: 500 },
    );
  }
}

export async function DELETE(_request: Request, { params }: RouteParams) {
  const { id } = await params;
  return proxyToBackend(`/${id}`, { method: "DELETE" });
}