package no.nav.syfo.service

import io.ktor.util.KtorExperimentalAPI
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import net.logstash.logback.argument.StructuredArguments
import no.nav.syfo.LoggingMeta
import no.nav.syfo.client.OppgaveClient
import no.nav.syfo.log
import no.nav.syfo.metrics.OPPRETT_OPPGAVE_COUNTER
import no.nav.syfo.model.OpprettOppgave
import no.nav.syfo.sak.avro.ProduceTask
import no.nav.syfo.sak.avro.RegisterJournal
import no.nav.syfo.wrapExceptions

@KtorExperimentalAPI
suspend fun handleRegisterOppgaveRequest(
    oppgaveClient: OppgaveClient,
    produceTask: ProduceTask,
    registerJournal: RegisterJournal,
    loggingMeta: LoggingMeta
) {
    wrapExceptions(loggingMeta) {
        log.info("Received a SM2013, going to create oppgave, {}", StructuredArguments.fields(loggingMeta))
        val opprettOppgave = if (produceTask.messageId == "96ea3c4e-de24-46b1-8ff0-ae757b90306b" || produceTask.messageId == "364b393f-e0d3-4776-83b8-8f7ba9438124") {
            log.info("Oppretter oppgave på enhet 0393")
            OpprettOppgave(
                aktoerId = produceTask.aktoerId,
                opprettetAvEnhetsnr = produceTask.opprettetAvEnhetsnr,
                tildeltEnhetsnr = "0393",
                journalpostId = registerJournal.journalpostId,
                behandlesAvApplikasjon = produceTask.behandlesAvApplikasjon,
                saksreferanse = registerJournal.sakId,
                beskrivelse = produceTask.beskrivelse,
                tema = produceTask.tema,
                oppgavetype = produceTask.oppgavetype,
                aktivDato = LocalDate.parse(produceTask.aktivDato, DateTimeFormatter.ISO_DATE),
                fristFerdigstillelse = LocalDate.parse(produceTask.fristFerdigstillelse, DateTimeFormatter.ISO_DATE),
                prioritet = produceTask.prioritet.name
            )
        } else {
            OpprettOppgave(
                aktoerId = produceTask.aktoerId,
                opprettetAvEnhetsnr = produceTask.opprettetAvEnhetsnr,
                journalpostId = registerJournal.journalpostId,
                behandlesAvApplikasjon = produceTask.behandlesAvApplikasjon,
                saksreferanse = registerJournal.sakId,
                beskrivelse = produceTask.beskrivelse,
                tema = produceTask.tema,
                oppgavetype = produceTask.oppgavetype,
                aktivDato = LocalDate.parse(produceTask.aktivDato, DateTimeFormatter.ISO_DATE),
                fristFerdigstillelse = LocalDate.parse(produceTask.fristFerdigstillelse, DateTimeFormatter.ISO_DATE),
                prioritet = produceTask.prioritet.name
            )
        }

        val oppgaveResultat = oppgaveClient.opprettOppgave(opprettOppgave, registerJournal.messageId, loggingMeta)
        if (!oppgaveResultat.duplikat) {
            OPPRETT_OPPGAVE_COUNTER.inc()
            log.info("Opprettet oppgave med {}, {}, {}, {}",
                    StructuredArguments.keyValue("oppgaveId", oppgaveResultat.oppgaveId),
                    StructuredArguments.keyValue("sakid", registerJournal.sakId),
                    StructuredArguments.keyValue("journalpost", registerJournal.journalpostId),
                    StructuredArguments.fields(loggingMeta))
        }
    }
}
