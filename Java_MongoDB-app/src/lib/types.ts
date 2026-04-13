export type Formulario = {
  id: string;
  nome: string;
  email: string;
  cep: string;
  logradouro: string;
  complemento: string;
  unidade: string;
  bairro: string;
  localidade: string;
  uf: string;
  estado: string;
  regiao: string;
  ibge: string;
  gia: string;
  ddd: string;
  siafi: string;
};

export type FormularioPayload = Pick<
  Formulario,
  "nome" | "email" | "cep" | "complemento" | "unidade"
>;

export type ApiError = {
  timestamp: string;
  status: number;
  error: string;
  trace: string;
};

export const EMPTY_FORMULARIO_PAYLOAD: FormularioPayload = {
  nome: "",
  email: "",
  cep: "",
  complemento: "",
  unidade: "",
};