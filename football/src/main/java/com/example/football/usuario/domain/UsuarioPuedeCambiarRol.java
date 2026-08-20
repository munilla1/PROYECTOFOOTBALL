package com.example.football.usuario.domain;

public class UsuarioPuedeCambiarRol {

    private final Usuario usuarioSolicitante;
    private final Usuario usuarioObjetivo;
    private final Rol nuevoRol;

    public UsuarioPuedeCambiarRol(Usuario usuarioSolicitante, Usuario usuarioObjetivo, Rol nuevoRol) {
        this.usuarioSolicitante = usuarioSolicitante;
        this.usuarioObjetivo = usuarioObjetivo;
        this.nuevoRol = nuevoRol;
    }

    public boolean esValida() {
        // Regla 1: Solo admin puede cambiar roles
        if (usuarioSolicitante.rol() != Rol.ADMIN) {
            return false;
        }

        // Regla 2: No puede cambiar su propio rol
        if (usuarioSolicitante.id().equals(usuarioObjetivo.id())) {
            return false;
        }

        // Regla 3: Rol debe ser válido
        if (nuevoRol == null) {
            return false;
        }

        return true;
    }

    public String obtenerMensajeError() {
        if (usuarioSolicitante.rol() != Rol.ADMIN) {
            return "Se requiere rol admin";
        }
        if (usuarioSolicitante.id().equals(usuarioObjetivo.id())) {
            return "No puedes modificar tu propio rol";
        }
        if (nuevoRol == null) {
            return "Rol no puede ser nulo";
        }
        return "Especificación inválida";
    }
}
