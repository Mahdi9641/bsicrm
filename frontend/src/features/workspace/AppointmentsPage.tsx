"use client";
import AppointmentsWorkspace from "@/components/AppointmentsWorkspace";
import type { WorkspaceContext } from "./types";
export default function AppointmentsPage({
  filtered,
  rows,
  operations,
  day,
  busyAction,
  setDialog,
  setDay,
  action,
}: WorkspaceContext) {
  return (
    <AppointmentsWorkspace
      rows={filtered}
      day={day}
      onDay={setDay}
      editable={operations}
      busy={busyAction}
      onAction={action}
      onReschedule={(id) => setDialog({ type: "booking", appointmentId: id })}
      onCancel={(row) => setDialog({ type: "cancelAppointment", row })}
    />
  );
}
