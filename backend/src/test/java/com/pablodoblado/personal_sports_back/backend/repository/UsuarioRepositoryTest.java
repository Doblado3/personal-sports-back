package com.pablodoblado.personal_sports_back.backend.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.pablodoblado.personal_sports_back.backend.entity.Usuario;

@DataJpaTest
public class UsuarioRepositoryTest {
	
	@Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UsuarioRepository usuarioRepository;
	
	private Usuario usuarioTest;
	
	@BeforeEach
	void setup() {
		
		usuarioTest = new Usuario();
        usuarioTest.setNombre("testuser");
        usuarioTest.setEmail("testuser@example.com");
        usuarioTest.setFechaNacimiento(LocalDateTime.now());
        usuarioTest.setPassword("password");
        usuarioTest.setStravaAthleteId(32175004L);
        entityManager.persist(usuarioTest);
	}
	
	@Test
	void testFindByEmail() {
		
		String email = "testuser@example.com";
		Usuario usuario = usuarioRepository.findByEmail(email).orElse(null);
		
		assertThat(usuario).isNotNull();
		assertThat(usuario).isEqualTo(usuarioTest);
	}
	
	@Test
	void findByStravaAthleteId() {
		
		Long StravaId = 32175004L;
		Usuario usuario = usuarioRepository.findByStravaAthleteId(StravaId).orElse(null);
		
		assertThat(usuario).isNotNull();
		assertThat(usuario).isEqualTo(usuarioTest);
	}

}
