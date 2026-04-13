import { proxyToBackend } from "@/lib/backend-proxy";

type RouteParams = {
  params: Promise<{
    cep: string;
  }>;
};

export async function GET(_request: Request, { params }: RouteParams) {
  const { cep } = await params;
  return proxyToBackend(`/cep/${cep}`);
}