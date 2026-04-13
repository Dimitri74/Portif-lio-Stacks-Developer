"use client";

import {
  AlertCircle,
  BadgeCheck,
  Building2,
  Layers3,
  LoaderCircle,
  Mail,
  MapPinHouse,
  PencilLine,
  Plus,
  RefreshCcw,
  Save,
  Search,
  Sparkles,
  Trash2,
} from "lucide-react";
import {
  startTransition,
  useCallback,
  useDeferredValue,
  useEffect,
  useState,
  useTransition,
  type FormEvent,
  type ReactNode,
} from "react";

import {
  EMPTY_FORMULARIO_PAYLOAD,
  type ApiError,
  type Formulario,
  type FormularioPayload,
} from "@/lib/types";

type Notice = {
  type: "success" | "error" | "info";
  message: string;
};

const initialNotice: Notice = {
  type: "info",
  message:
    "A dashboard usa o Next.js como proxy do backend para evitar problemas de CORS com a API Java.",
};

function formatCep(value: string) {
  const digits = value.replace(/\D/g, "").slice(0, 8);

  if (digits.length <= 5) {
    return digits;
  }

  return `${digits.slice(0, 5)}-${digits.slice(5)}`;
}

function getErrorMessage(error: unknown, fallback: string) {
  if (
    typeof error === "object" &&
    error !== null &&
    "trace" in error &&
    typeof error.trace === "string"
  ) {
    return error.trace;
  }

  return fallback;
}

async function request<T>(input: RequestInfo, init?: RequestInit): Promise<T> {
  const response = await fetch(input, {
    ...init,
    headers: {
      "Content-Type": "application/json",
      ...(init?.headers ?? {}),
    },
  });

  if (response.status === 204) {
    return undefined as T;
  }

  const data = (await response.json()) as T | ApiError;

  if (!response.ok) {
    throw data;
  }

  return data as T;
}

function toEditablePayload(formulario: Formulario): FormularioPayload {
  return {
    nome: formulario.nome ?? "",
    email: formulario.email ?? "",
    cep: formulario.cep ?? "",
    complemento: formulario.complemento ?? "",
    unidade: formulario.unidade ?? "",
  };
}

function StatCard({
  title,
  value,
  helper,
  icon,
}: {
  title: string;
  value: string;
  helper: string;
  icon: ReactNode;
}) {
  return (
    <article className="glass-card card-outline animate-fade-up rounded-[28px] p-5">
      <div className="mb-4 inline-flex rounded-2xl bg-accent-soft p-3 text-accent">
        {icon}
      </div>
      <p className="text-sm uppercase tracking-[0.18em] text-muted">{title}</p>
      <p className="mt-2 text-3xl font-semibold text-foreground">{value}</p>
      <p className="mt-2 text-sm text-muted">{helper}</p>
    </article>
  );
}

export function FormularioDashboard() {
  const [formularios, setFormularios] = useState<Formulario[]>([]);
  const [form, setForm] = useState<FormularioPayload>(EMPTY_FORMULARIO_PAYLOAD);
  const [selectedId, setSelectedId] = useState<string | null>(null);
  const [selectedEndereco, setSelectedEndereco] = useState<Formulario | null>(null);
  const [search, setSearch] = useState("");
  const [notice, setNotice] = useState<Notice>(initialNotice);
  const [cepPreview, setCepPreview] = useState<Partial<Formulario> | null>(null);
  const [isLoading, startLoadingTransition] = useTransition();
  const [isSaving, startSavingTransition] = useTransition();
  const [isLookingUpCep, setIsLookingUpCep] = useState(false);
  const deferredSearch = useDeferredValue(search.trim().toLowerCase());

  const filteredFormularios = formularios.filter((formulario) => {
    if (!deferredSearch) {
      return true;
    }

    const searchable = [
      formulario.nome,
      formulario.email,
      formulario.localidade,
      formulario.uf,
      formulario.cep,
    ]
      .join(" ")
      .toLowerCase();

    return searchable.includes(deferredSearch);
  });

  const totalCidades = new Set(
    formularios.map((formulario) => formulario.localidade).filter(Boolean),
  ).size;
  const totalEstados = new Set(
    formularios.map((formulario) => formulario.uf).filter(Boolean),
  ).size;

  const loadFormularios = useCallback(
    async (options?: { preserveSelection?: boolean }) => {
    try {
      const data = await request<Formulario[]>("/api/formularios", {
        method: "GET",
      });

      setFormularios(data);

      if (!options?.preserveSelection || !selectedId) {
        return;
      }

      const updatedSelection = data.find((item) => item.id === selectedId) ?? null;
      setSelectedEndereco(updatedSelection);

      if (!updatedSelection) {
        setSelectedId(null);
        setForm(EMPTY_FORMULARIO_PAYLOAD);
        setCepPreview(null);
      }
    } catch (error) {
      setNotice({
        type: "error",
        message: getErrorMessage(
          error,
          "Nao foi possivel carregar a lista de formularios.",
        ),
      });
    }
    },
    [selectedId],
  );

  useEffect(() => {
    startLoadingTransition(() => {
      void loadFormularios();
    });
  }, [loadFormularios]);

  function resetForm() {
    setForm(EMPTY_FORMULARIO_PAYLOAD);
    setSelectedId(null);
    setSelectedEndereco(null);
    setCepPreview(null);
  }

  function handleChange<K extends keyof FormularioPayload>(
    key: K,
    value: FormularioPayload[K],
  ) {
    setForm((current) => ({
      ...current,
      [key]: key === "cep" && typeof value === "string" ? formatCep(value) : value,
    }));
  }

  async function handleCepLookup() {
    const cepDigits = form.cep.replace(/\D/g, "");

    if (cepDigits.length !== 8) {
      setNotice({
        type: "error",
        message: "Informe um CEP com 8 digitos para consultar o endereco.",
      });
      return;
    }

    setIsLookingUpCep(true);

    try {
      const data = await request<Partial<Formulario>>(`/api/formularios/cep/${cepDigits}`);
      setCepPreview(data);
      setNotice({
        type: "success",
        message: "Endereco encontrado. O backend vai enriquecer o formulario com esses dados ao salvar.",
      });
    } catch (error) {
      setCepPreview(null);
      setNotice({
        type: "error",
        message: getErrorMessage(error, "Nao foi possivel consultar o CEP."),
      });
    } finally {
      setIsLookingUpCep(false);
    }
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();

    startSavingTransition(() => {
      void (async () => {
        try {
          const payload = {
            ...form,
            cep: form.cep.replace(/\D/g, ""),
          };

          const endpoint = selectedId ? `/api/formularios/${selectedId}` : "/api/formularios";
          const method = selectedId ? "PUT" : "POST";

          const saved = await request<Formulario>(endpoint, {
            method,
            body: JSON.stringify(payload),
          });

          setNotice({
            type: "success",
            message: selectedId
              ? "Formulario atualizado com sucesso."
              : "Formulario criado com sucesso.",
          });

          setSelectedId(saved.id);
          setSelectedEndereco(saved);
          setForm(toEditablePayload(saved));
          setCepPreview(saved);

          startTransition(() => {
            void loadFormularios({ preserveSelection: true });
          });
        } catch (error) {
          setNotice({
            type: "error",
            message: getErrorMessage(error, "Nao foi possivel salvar o formulario."),
          });
        }
      })();
    });
  }

  async function handleDelete(id: string) {
    const confirmed = window.confirm(
      "Deseja realmente excluir este formulario? Esta acao nao pode ser desfeita.",
    );

    if (!confirmed) {
      return;
    }

    startLoadingTransition(() => {
      void (async () => {
        try {
          await request<void>(`/api/formularios/${id}`, {
            method: "DELETE",
          });

          if (selectedId === id) {
            resetForm();
          }

          setNotice({
            type: "success",
            message: "Formulario removido com sucesso.",
          });

          await loadFormularios({ preserveSelection: true });
        } catch (error) {
          setNotice({
            type: "error",
            message: getErrorMessage(error, "Nao foi possivel excluir o formulario."),
          });
        }
      })();
    });
  }

  function handleEdit(formulario: Formulario) {
    setSelectedId(formulario.id);
    setSelectedEndereco(formulario);
    setForm(toEditablePayload(formulario));
    setCepPreview(formulario);
    setNotice({
      type: "info",
      message: `Edicao carregada para ${formulario.nome}. Salve para persistir alteracoes no MongoDB.`,
    });
  }

  const noticeStyles =
    notice.type === "success"
      ? "border-emerald-200 bg-emerald-50 text-emerald-900"
      : notice.type === "error"
        ? "border-red-200 bg-red-50 text-red-900"
        : "border-white/60 bg-white/80 text-foreground";

  return (
    <main className="mx-auto flex min-h-screen w-full max-w-[1500px] flex-col px-4 py-6 sm:px-6 lg:px-8">
      <section className="animate-fade-up overflow-hidden rounded-[36px] border border-white/60 bg-[linear-gradient(135deg,rgba(255,251,245,0.92),rgba(255,241,222,0.88))] px-6 py-8 shadow-[0_24px_90px_rgba(82,52,22,0.12)] sm:px-8 lg:px-10">
        <div className="flex flex-col gap-8 lg:flex-row lg:items-end lg:justify-between">
          <div className="max-w-3xl">
            <div className="inline-flex items-center gap-2 rounded-full border border-white/70 bg-white/70 px-4 py-2 text-xs font-medium uppercase tracking-[0.25em] text-accent-strong">
              <Sparkles className="size-4" />
              Dashboard 
            </div>
            <h1 className="mt-5 max-w-3xl text-4xl font-semibold leading-tight tracking-[-0.04em] text-foreground sm:text-5xl lg:text-6xl">
              CRUD de formularios com visual de cockpit e fluxo ligado ao Spring Boot e React.
            </h1>
            <p className="mt-4 max-w-2xl text-base leading-8 text-muted sm:text-lg">
              O painel lista dados da collection <span className="font-mono">formularios</span>,
              consulta CEP via a mesma API Java e usa rotas internas do Next.js para evitar
              CORS no navegador.
            </p>
          </div>

          <div className="grid gap-3 rounded-[28px] border border-white/70 bg-white/70 p-4 text-sm text-muted shadow-[0_18px_50px_rgba(15,118,110,0.08)] sm:grid-cols-2">
            <div>
              <p className="text-xs uppercase tracking-[0.2em] text-muted">Backend alvo</p>
              <p className="mt-2 font-mono text-sm text-foreground">
                http://localhost:8080/api/formularios
              </p>
            </div>
            <div>
              <p className="text-xs uppercase tracking-[0.2em] text-muted">Stack</p>
              <p className="mt-2 text-foreground">Next 16, React 19, Tailwind 4</p>
            </div>
          </div>
        </div>
      </section>

      <section className="mt-6 grid gap-4 md:grid-cols-3">
        <StatCard
          title="Registros"
          value={String(formularios.length).padStart(2, "0")}
          helper="Total de documentos carregados do MongoDB"
          icon={<Layers3 className="size-5" />}
        />
        <StatCard
          title="Cidades"
          value={String(totalCidades).padStart(2, "0")}
          helper="Cobertura territorial encontrada nos cadastros"
          icon={<MapPinHouse className="size-5" />}
        />
        <StatCard
          title="Estados"
          value={String(totalEstados).padStart(2, "0")}
          helper="UFs diferentes enriquecidas pelo ViaCEP"
          icon={<Building2 className="size-5" />}
        />
      </section>

      <div className="mt-6 grid gap-6 xl:grid-cols-[420px_minmax(0,1fr)]">
        <section className="glass-card card-outline animate-fade-up rounded-[32px] p-6">
          <div className="flex items-start justify-between gap-4">
            <div>
              <p className="text-sm uppercase tracking-[0.24em] text-muted">
                {selectedId ? "Modo edicao" : "Novo cadastro"}
              </p>
              <h2 className="mt-2 text-2xl font-semibold tracking-[-0.03em]">
                {selectedId ? "Atualizar formulario" : "Criar formulario"}
              </h2>
            </div>

            <button
              type="button"
              onClick={() => {
                resetForm();
                setNotice(initialNotice);
              }}
              className="rounded-full border border-line bg-white px-4 py-2 text-sm text-foreground hover:-translate-y-0.5 hover:border-accent/30"
            >
              Limpar
            </button>
          </div>

          <form className="mt-6 space-y-4" onSubmit={handleSubmit}>
            <label className="block space-y-2">
              <span className="text-sm font-medium text-foreground">Nome</span>
              <input
                value={form.nome}
                onChange={(event) => handleChange("nome", event.target.value)}
                className="w-full rounded-2xl border border-line bg-white/85 px-4 py-3 text-sm text-foreground placeholder:text-muted focus:border-accent focus:ring-4 focus:ring-accent/10"
                placeholder="Marcus Dimitri"
                required
              />
            </label>

            <label className="block space-y-2">
              <span className="text-sm font-medium text-foreground">E-mail</span>
              <input
                type="email"
                value={form.email}
                onChange={(event) => handleChange("email", event.target.value)}
                className="w-full rounded-2xl border border-line bg-white/85 px-4 py-3 text-sm text-foreground placeholder:text-muted focus:border-accent focus:ring-4 focus:ring-accent/10"
                placeholder="marcus@email.com"
                required
              />
            </label>

            <div className="grid gap-4 sm:grid-cols-[minmax(0,1fr)_auto] sm:items-end">
              <label className="block space-y-2">
                <span className="text-sm font-medium text-foreground">CEP</span>
                <input
                  value={form.cep}
                  onChange={(event) => handleChange("cep", event.target.value)}
                  className="w-full rounded-2xl border border-line bg-white/85 px-4 py-3 text-sm text-foreground placeholder:text-muted focus:border-accent focus:ring-4 focus:ring-accent/10"
                  placeholder="01001-000"
                  maxLength={9}
                  required
                />
              </label>

              <button
                type="button"
                onClick={handleCepLookup}
                disabled={isLookingUpCep}
                className="animate-pulse-border inline-flex items-center justify-center gap-2 rounded-2xl bg-accent px-4 py-3 text-sm font-medium text-white hover:-translate-y-0.5 hover:bg-accent-strong disabled:cursor-wait disabled:opacity-70"
              >
                {isLookingUpCep ? (
                  <LoaderCircle className="size-4 animate-spin" />
                ) : (
                  <Search className="size-4" />
                )}
                Buscar CEP
              </button>
            </div>

            <div className="grid gap-4 sm:grid-cols-2">
              <label className="block space-y-2">
                <span className="text-sm font-medium text-foreground">Complemento</span>
                <input
                  value={form.complemento}
                  onChange={(event) => handleChange("complemento", event.target.value)}
                  className="w-full rounded-2xl border border-line bg-white/85 px-4 py-3 text-sm text-foreground placeholder:text-muted focus:border-accent focus:ring-4 focus:ring-accent/10"
                  placeholder="Apto 42"
                />
              </label>

              <label className="block space-y-2">
                <span className="text-sm font-medium text-foreground">Unidade</span>
                <input
                  value={form.unidade}
                  onChange={(event) => handleChange("unidade", event.target.value)}
                  className="w-full rounded-2xl border border-line bg-white/85 px-4 py-3 text-sm text-foreground placeholder:text-muted focus:border-accent focus:ring-4 focus:ring-accent/10"
                  placeholder="Bloco B"
                />
              </label>
            </div>

            <div className="rounded-[28px] border border-dashed border-line bg-white/65 p-4">
              <div className="flex items-center gap-2 text-sm font-medium text-foreground">
                <BadgeCheck className="size-4 text-accent" />
                Endereco enriquecido pelo backend
              </div>

              {cepPreview ? (
                <div className="mt-4 grid gap-3 text-sm text-muted sm:grid-cols-2">
                  <p>
                    <span className="block text-xs uppercase tracking-[0.18em] text-muted">
                      Logradouro
                    </span>
                    <span className="mt-1 block text-foreground">
                      {cepPreview.logradouro || "Nao informado"}
                    </span>
                  </p>
                  <p>
                    <span className="block text-xs uppercase tracking-[0.18em] text-muted">
                      Bairro
                    </span>
                    <span className="mt-1 block text-foreground">
                      {cepPreview.bairro || "Nao informado"}
                    </span>
                  </p>
                  <p>
                    <span className="block text-xs uppercase tracking-[0.18em] text-muted">
                      Cidade
                    </span>
                    <span className="mt-1 block text-foreground">
                      {cepPreview.localidade || "Nao informado"}
                    </span>
                  </p>
                  <p>
                    <span className="block text-xs uppercase tracking-[0.18em] text-muted">
                      UF
                    </span>
                    <span className="mt-1 block text-foreground">
                      {cepPreview.uf || "Nao informado"}
                    </span>
                  </p>
                </div>
              ) : (
                <p className="mt-3 text-sm leading-7 text-muted">
                  Consulte um CEP para visualizar o endereco antes do envio. No cadastro real,
                  o Spring Boot continua sendo a fonte de verdade para preenchimento e validacao.
                </p>
              )}
            </div>

            <button
              type="submit"
              disabled={isSaving}
              className="inline-flex w-full items-center justify-center gap-2 rounded-2xl bg-foreground px-5 py-4 text-sm font-medium text-white hover:-translate-y-0.5 hover:bg-[#111814] disabled:cursor-wait disabled:opacity-70"
            >
              {isSaving ? (
                <LoaderCircle className="size-4 animate-spin" />
              ) : selectedId ? (
                <Save className="size-4" />
              ) : (
                <Plus className="size-4" />
              )}
              {selectedId ? "Salvar alteracoes" : "Criar formulario"}
            </button>
          </form>
        </section>

        <section className="glass-card card-outline animate-fade-up rounded-[32px] p-6 [animation-delay:120ms]">
          <div className="flex flex-col gap-4 xl:flex-row xl:items-center xl:justify-between">
            <div>
              <p className="text-sm uppercase tracking-[0.24em] text-muted">Visao operacional</p>
              <h2 className="mt-2 text-2xl font-semibold tracking-[-0.03em] text-foreground">
                Registros ativos na collection
              </h2>
            </div>

            <div className="flex flex-col gap-3 sm:flex-row">
              <label className="relative min-w-[240px]">
                <Search className="pointer-events-none absolute left-4 top-1/2 size-4 -translate-y-1/2 text-muted" />
                <input
                  value={search}
                  onChange={(event) => setSearch(event.target.value)}
                  className="w-full rounded-2xl border border-line bg-white/85 py-3 pl-11 pr-4 text-sm text-foreground placeholder:text-muted focus:border-accent focus:ring-4 focus:ring-accent/10"
                  placeholder="Filtrar por nome, email, cidade ou CEP"
                />
              </label>
              <button
                type="button"
                onClick={() => {
                  startTransition(() => {
                    void loadFormularios({ preserveSelection: true });
                  });
                }}
                className="inline-flex items-center justify-center gap-2 rounded-2xl border border-line bg-white px-4 py-3 text-sm text-foreground hover:-translate-y-0.5 hover:border-accent/30"
              >
                <RefreshCcw className={`size-4 ${isLoading ? "animate-spin" : ""}`} />
                Atualizar dados
              </button>
            </div>
          </div>

          <div className={`mt-5 rounded-2xl border px-4 py-3 text-sm ${noticeStyles}`}>
            <div className="flex items-start gap-3">
              <AlertCircle className="mt-0.5 size-4 shrink-0" />
              <p className="leading-7">{notice.message}</p>
            </div>
          </div>

          <div className="mt-5 hidden overflow-hidden rounded-[28px] border border-line xl:block">
            <table className="min-w-full bg-white/70 text-left">
              <thead className="bg-white/80 text-xs uppercase tracking-[0.22em] text-muted">
                <tr>
                  <th className="px-5 py-4 font-medium">Pessoa</th>
                  <th className="px-5 py-4 font-medium">Contato</th>
                  <th className="px-5 py-4 font-medium">Local</th>
                  <th className="px-5 py-4 font-medium">Acoes</th>
                </tr>
              </thead>
              <tbody>
                {filteredFormularios.length === 0 ? (
                  <tr>
                    <td colSpan={4} className="px-5 py-10 text-center text-sm text-muted">
                      Nenhum formulario encontrado com o filtro atual.
                    </td>
                  </tr>
                ) : (
                  filteredFormularios.map((formulario) => (
                    <tr key={formulario.id} className="border-t border-line/80 text-sm text-foreground">
                      <td className="px-5 py-4 align-top">
                        <p className="font-medium">{formulario.nome}</p>
                        <p className="mt-1 font-mono text-xs text-muted">ID {formulario.id}</p>
                      </td>
                      <td className="px-5 py-4 align-top">
                        <div className="inline-flex items-center gap-2 text-muted">
                          <Mail className="size-4" />
                          {formulario.email}
                        </div>
                        <p className="mt-2 font-mono text-xs text-muted">CEP {formulario.cep}</p>
                      </td>
                      <td className="px-5 py-4 align-top">
                        <p>{formulario.localidade || "Cidade nao informada"}</p>
                        <p className="mt-1 text-muted">
                          {[formulario.bairro, formulario.uf].filter(Boolean).join(" • ") ||
                            "Endereco incompleto"}
                        </p>
                      </td>
                      <td className="px-5 py-4 align-top">
                        <div className="flex gap-2">
                          <button
                            type="button"
                            onClick={() => handleEdit(formulario)}
                            className="inline-flex items-center gap-2 rounded-full border border-line bg-white px-3 py-2 text-xs font-medium text-foreground hover:-translate-y-0.5 hover:border-accent/30"
                          >
                            <PencilLine className="size-3.5" />
                            Editar
                          </button>
                          <button
                            type="button"
                            onClick={() => handleDelete(formulario.id)}
                            className="inline-flex items-center gap-2 rounded-full border border-red-200 bg-red-50 px-3 py-2 text-xs font-medium text-red-700 hover:-translate-y-0.5 hover:bg-red-100"
                          >
                            <Trash2 className="size-3.5" />
                            Excluir
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>

          <div className="mt-5 grid gap-4 xl:hidden">
            {filteredFormularios.length === 0 ? (
              <div className="rounded-[28px] border border-line bg-white/70 px-5 py-8 text-center text-sm text-muted">
                Nenhum formulario encontrado com o filtro atual.
              </div>
            ) : (
              filteredFormularios.map((formulario) => (
                <article key={formulario.id} className="rounded-[28px] border border-line bg-white/75 p-5">
                  <div className="flex items-start justify-between gap-4">
                    <div>
                      <p className="text-lg font-medium text-foreground">{formulario.nome}</p>
                      <p className="mt-1 font-mono text-xs text-muted">{formulario.id}</p>
                    </div>
                    <button
                      type="button"
                      onClick={() => handleEdit(formulario)}
                      className="inline-flex items-center gap-2 rounded-full border border-line bg-white px-3 py-2 text-xs font-medium text-foreground"
                    >
                      <PencilLine className="size-3.5" />
                      Abrir
                    </button>
                  </div>
                  <div className="mt-4 space-y-2 text-sm text-muted">
                    <p>{formulario.email}</p>
                    <p>
                      {[formulario.logradouro, formulario.bairro].filter(Boolean).join(" • ") ||
                        "Endereco ainda nao enriquecido"}
                    </p>
                    <p>
                      {[formulario.localidade, formulario.uf, formulario.cep]
                        .filter(Boolean)
                        .join(" • ")}
                    </p>
                  </div>
                  <div className="mt-4 flex gap-2">
                    <button
                      type="button"
                      onClick={() => handleEdit(formulario)}
                      className="inline-flex flex-1 items-center justify-center gap-2 rounded-2xl border border-line bg-white px-4 py-3 text-sm font-medium text-foreground"
                    >
                      <PencilLine className="size-4" />
                      Editar
                    </button>
                    <button
                      type="button"
                      onClick={() => handleDelete(formulario.id)}
                      className="inline-flex flex-1 items-center justify-center gap-2 rounded-2xl border border-red-200 bg-red-50 px-4 py-3 text-sm font-medium text-red-700"
                    >
                      <Trash2 className="size-4" />
                      Excluir
                    </button>
                  </div>
                </article>
              ))
            )}
          </div>

          <div className="mt-6 rounded-[28px] border border-line bg-[linear-gradient(135deg,rgba(15,118,110,0.12),rgba(255,255,255,0.85))] p-5">
            <p className="text-sm uppercase tracking-[0.22em] text-muted">Registro em foco</p>
            {selectedEndereco ? (
              <div className="mt-3 grid gap-3 text-sm text-muted sm:grid-cols-2">
                <p>
                  <span className="block text-xs uppercase tracking-[0.18em] text-muted">Nome</span>
                  <span className="mt-1 block text-foreground">{selectedEndereco.nome}</span>
                </p>
                <p>
                  <span className="block text-xs uppercase tracking-[0.18em] text-muted">Email</span>
                  <span className="mt-1 block text-foreground">{selectedEndereco.email}</span>
                </p>
                <p>
                  <span className="block text-xs uppercase tracking-[0.18em] text-muted">Endereco</span>
                  <span className="mt-1 block text-foreground">
                    {[
                      selectedEndereco.logradouro,
                      selectedEndereco.bairro,
                      selectedEndereco.localidade,
                      selectedEndereco.uf,
                    ]
                      .filter(Boolean)
                      .join(" • ") || "Nao informado"}
                  </span>
                </p>
                <p>
                  <span className="block text-xs uppercase tracking-[0.18em] text-muted">Complemento</span>
                  <span className="mt-1 block text-foreground">
                    {selectedEndereco.complemento || "Nao informado"}
                  </span>
                </p>
              </div>
            ) : (
              <p className="mt-3 text-sm leading-7 text-muted">
                Selecione um registro para revisar o payload persistido. O backend continua
                aplicando a regra de email unico e o enriquecimento do endereco.
              </p>
            )}
          </div>
        </section>
      </div>
    </main>
  );
}