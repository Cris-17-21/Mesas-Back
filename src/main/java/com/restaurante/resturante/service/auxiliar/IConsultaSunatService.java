package com.restaurante.resturante.service.auxiliar;

import com.restaurante.resturante.dto.auxiliar.EmpresaDto;
import com.restaurante.resturante.dto.auxiliar.PersonaDto;

public interface IConsultaSunatService {

    PersonaDto consultarDni(String dni);
    
    EmpresaDto consultarRuc(String ruc);
}
