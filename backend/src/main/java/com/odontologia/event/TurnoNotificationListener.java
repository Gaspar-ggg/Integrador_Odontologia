package com.odontologia.event;

import com.odontologia.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TurnoNotificationListener {

    private final List<NotificationService> notificationChannels;

    @Async("notificationTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onTurnoConfirmado(TurnoConfirmadoEvent event) {
        for (NotificationService channel : notificationChannels) {
            try {
                channel.enviarConfirmacionTurno(event);
            } catch (Exception ex) {
                log.error("[NOTIF-ERROR] Falla no controlada en canal {} para Turno #{}: {}",
                        channel.getCanal(), event.turnoId(), ex.getMessage());
            }
        }
    }
}