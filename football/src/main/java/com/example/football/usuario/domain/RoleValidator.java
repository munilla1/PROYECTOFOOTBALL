package com.example.football.usuario.domain;

public class RoleValidator {

    public void validarRolValido(String rolString) {
        try {
            Rol.desde(rolString);
        } catch (IllegalArgumentException e) {
            throw new RolInvalidoException(e.getMessage());
        }
    }

    public void validarRolValido(Rol rol) {
        if (rol == null) {
            throw new RolInvalidoException("Rol no puede ser nulo");
        }
    }

    public void validarCambioDeRol(Usuario usuario, Rol nuevoRol, Usuario adminSolicitante) {
        // Validar que el solicitante es admin
        if (adminSolicitante.rol() != Rol.ADMIN) {
            throw new NoAutorizadoParaCambiarRolException("Se requiere rol admin");
        }

        // Validar que no intenta cambiar su propio rol
        if (usuario.id().equals(adminSolicitante.id())) {
            throw new NoAutorizadoParaCambiarRolException("No puedes modificar tu propio rol");
        }

        // Validar que el rol nuevo es válido
        validarRolValido(nuevoRol);
    }
}
