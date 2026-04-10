package com.example.formulario.Java_MongoDB.service;

import java.util.List;
import java.util.regex.Pattern;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.formulario.Java_MongoDB.client.ViaCepClient;
import com.example.formulario.Java_MongoDB.dto.FormularioDTO;
import com.example.formulario.Java_MongoDB.dto.ViaCepResponseDTO;
import com.example.formulario.Java_MongoDB.model.Formulario;
import com.example.formulario.Java_MongoDB.repository.FormularioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FormularioService {

	private static final Pattern CEP_PATTERN = Pattern.compile("\\d{8}");

	private final FormularioRepository formularioRepository;
	private final ViaCepClient viaCepClient;

	public FormularioDTO criar(FormularioDTO formularioDTO) {
		FormularioDTO formularioEnriquecido = enriquecerComCep(formularioDTO);
		Formulario salvo = formularioRepository.save(toEntity(formularioEnriquecido));
		return toDTO(salvo);
	}

	public List<FormularioDTO> listar() {
		return formularioRepository.findAll().stream().map(this::toDTO).toList();
	}

	public FormularioDTO buscarPorId(String id) {
		Formulario formulario = formularioRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Formulario nao encontrado"));
		return toDTO(formulario);
	}

	public FormularioDTO buscarEnderecoPorCep(String cep) {
		String cepNormalizado = normalizarCep(cep);
		ViaCepResponseDTO endereco = viaCepClient.buscarEnderecoPorCep(cepNormalizado);

		if (Boolean.TRUE.equals(endereco.getErro())) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "CEP nao encontrado");
		}

		return FormularioDTO.builder()
				.cep(endereco.getCep())
				.logradouro(endereco.getLogradouro())
				.complemento(endereco.getComplemento())
				.unidade(endereco.getUnidade())
				.bairro(endereco.getBairro())
				.localidade(endereco.getLocalidade())
				.uf(endereco.getUf())
				.estado(endereco.getEstado())
				.regiao(endereco.getRegiao())
				.ibge(endereco.getIbge())
				.gia(endereco.getGia())
				.ddd(endereco.getDdd())
				.siafi(endereco.getSiafi())
				.build();
	}

	public FormularioDTO atualizar(String id, FormularioDTO formularioDTO) {
		Formulario existente = formularioRepository.findById(id)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Formulario nao encontrado"));
		FormularioDTO formularioEnriquecido = enriquecerComCep(formularioDTO);

		existente.setNome(formularioEnriquecido.getNome());
		existente.setEmail(formularioEnriquecido.getEmail());
		existente.setCep(formularioEnriquecido.getCep());
		existente.setLogradouro(formularioEnriquecido.getLogradouro());
		existente.setComplemento(formularioEnriquecido.getComplemento());
		existente.setUnidade(formularioEnriquecido.getUnidade());
		existente.setBairro(formularioEnriquecido.getBairro());
		existente.setLocalidade(formularioEnriquecido.getLocalidade());
		existente.setUf(formularioEnriquecido.getUf());
		existente.setEstado(formularioEnriquecido.getEstado());
		existente.setRegiao(formularioEnriquecido.getRegiao());
		existente.setIbge(formularioEnriquecido.getIbge());
		existente.setGia(formularioEnriquecido.getGia());
		existente.setDdd(formularioEnriquecido.getDdd());
		existente.setSiafi(formularioEnriquecido.getSiafi());

		Formulario atualizado = formularioRepository.save(existente);
		return toDTO(atualizado);
	}

	public void deletar(String id) {
		if (!formularioRepository.existsById(id)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Formulario nao encontrado");
		}
		formularioRepository.deleteById(id);
	}

	private FormularioDTO enriquecerComCep(FormularioDTO formularioDTO) {
		if (formularioDTO.getCep() == null || formularioDTO.getCep().isBlank()) {
			return formularioDTO;
		}

		FormularioDTO endereco = buscarEnderecoPorCep(formularioDTO.getCep());

		return FormularioDTO.builder()
				.id(formularioDTO.getId())
				.nome(formularioDTO.getNome())
				.email(formularioDTO.getEmail())
				.cep(endereco.getCep())
				.logradouro(endereco.getLogradouro())
				.complemento(valorPreferencial(formularioDTO.getComplemento(), endereco.getComplemento()))
				.unidade(valorPreferencial(formularioDTO.getUnidade(), endereco.getUnidade()))
				.bairro(endereco.getBairro())
				.localidade(endereco.getLocalidade())
				.uf(endereco.getUf())
				.estado(endereco.getEstado())
				.regiao(endereco.getRegiao())
				.ibge(endereco.getIbge())
				.gia(endereco.getGia())
				.ddd(endereco.getDdd())
				.siafi(endereco.getSiafi())
				.build();
	}

	private String normalizarCep(String cep) {
		String cepNormalizado = cep == null ? "" : cep.replaceAll("\\D", "");

		if (!CEP_PATTERN.matcher(cepNormalizado).matches()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CEP invalido. Informe 8 digitos.");
		}

		return cepNormalizado;
	}

	private String valorPreferencial(String valorFormulario, String valorViaCep) {
		if (valorFormulario != null && !valorFormulario.isBlank()) {
			return valorFormulario;
		}
		return valorViaCep;
	}

	private FormularioDTO toDTO(Formulario formulario) {
		return FormularioDTO.builder()
				.id(formulario.getId())
				.nome(formulario.getNome())
				.email(formulario.getEmail())
				.cep(formulario.getCep())
				.logradouro(formulario.getLogradouro())
				.complemento(formulario.getComplemento())
				.unidade(formulario.getUnidade())
				.bairro(formulario.getBairro())
				.localidade(formulario.getLocalidade())
				.uf(formulario.getUf())
				.estado(formulario.getEstado())
				.regiao(formulario.getRegiao())
				.ibge(formulario.getIbge())
				.gia(formulario.getGia())
				.ddd(formulario.getDdd())
				.siafi(formulario.getSiafi())
				.build();
	}

	private Formulario toEntity(FormularioDTO formularioDTO) {
		return Formulario.builder()
				.id(formularioDTO.getId())
				.nome(formularioDTO.getNome())
				.email(formularioDTO.getEmail())
				.cep(formularioDTO.getCep())
				.logradouro(formularioDTO.getLogradouro())
				.complemento(formularioDTO.getComplemento())
				.unidade(formularioDTO.getUnidade())
				.bairro(formularioDTO.getBairro())
				.localidade(formularioDTO.getLocalidade())
				.uf(formularioDTO.getUf())
				.estado(formularioDTO.getEstado())
				.regiao(formularioDTO.getRegiao())
				.ibge(formularioDTO.getIbge())
				.gia(formularioDTO.getGia())
				.ddd(formularioDTO.getDdd())
				.siafi(formularioDTO.getSiafi())
				.build();
	}
}

