package com.odontologia.model;

public enum EstadoTurno {
    PENDIENTE,
    CONFIRMADO,
    ATENDIDO,
    CANCELADO;

    public boolean puedeTransicionarA(EstadoTurno destino) {
        if (destino == null || this == destino) {
            return false;
        }
        return switch (this) {
            case PENDIENTE -> destino == CONFIRMADO || destino == CANCELADO;
            case CONFIRMADO -> destino == ATENDIDO || destino == CANCELADO;
            case ATENDIDO, CANCELADO -> false;
        };
    }

    public boolean bloqueaAgenda() {
        return this == PENDIENTE || this == CONFIRMADO || this == ATENDIDO;
    }
}
