package com.restaurante.resturante;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class ResturanteApplicationTests {

	@Test
	void contextLoads() {
		// Verifica que el contexto de Spring arranca correctamente con H2 en memoria.
		// Esto garantiza que no hay errores de configuración, beans mal definidos
		// ni dependencias circulares en la aplicación.
	}

}
