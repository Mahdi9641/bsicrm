"use client";
import TasksWorkspace from "@/components/TasksWorkspace";
import type { WorkspaceContext } from "./types";
export default function TasksPage({ user }: WorkspaceContext) {
  return <TasksWorkspace user={user} />;
}
