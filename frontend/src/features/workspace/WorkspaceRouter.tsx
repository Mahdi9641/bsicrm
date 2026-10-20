"use client";
import ManagementPage from "@/features/admin/ManagementPage";
import SubscriptionPage from "@/features/admin/SubscriptionPage";
import CommissionRulesPage from "@/features/finance/CommissionRulesPage";
import ResourcesPage from "@/features/operations/ResourcesPage";
import AppointmentsPage from "./AppointmentsPage";
import AuditPage from "./AuditPage";
import CampaignsPage from "./CampaignsPage";
import CommissionsPage from "./CommissionsPage";
import CustomersPage from "./CustomersPage";
import DashboardPage from "./DashboardPage";
import EventsPage from "./EventsPage";
import FinancePage from "./FinancePage";
import InvoicesPage from "./InvoicesPage";
import MessagesPage from "./MessagesPage";
import ProductsPage from "./ProductsPage";
import ReportsPage from "./ReportsPage";
import RfmPage from "./RfmPage";
import SecurityPage from "./SecurityPage";
import ServicesPage from "./ServicesPage";
import SettingsPage from "./SettingsPage";
import StaffPage from "./StaffPage";
import TasksPage from "./TasksPage";
import type { WorkspaceContext } from "./types";
import VisitsPage from "./VisitsPage";
export default function WorkspaceRouter(props: WorkspaceContext) {
  if (["salons","branches","users"].includes(props.tab)) return <ManagementPage section={props.tab} user={props.user} onChanged={props.done}/>;
  if (props.tab==="subscription") return <SubscriptionPage user={props.user} onChanged={props.done}/>;
  if (props.tab === "resources") return <ResourcesPage />;
  if (props.tab === "commission-rules")
    return (
      <CommissionRulesPage lookups={props.lookups} manager={props.manager} />
    );
  if (["tasks"].includes(props.tab)) return <TasksPage {...props} />;
  if (["security"].includes(props.tab)) return <SecurityPage {...props} />;
  if (["dashboard"].includes(props.tab)) return <DashboardPage {...props} />;
  if (["customers"].includes(props.tab)) return <CustomersPage {...props} />;
  if (["appointments"].includes(props.tab))
    return <AppointmentsPage {...props} />;
  if (["visits"].includes(props.tab)) return <VisitsPage {...props} />;
  if (["services"].includes(props.tab)) return <ServicesPage {...props} />;
  if (["products"].includes(props.tab)) return <ProductsPage {...props} />;
  if (["invoices"].includes(props.tab)) return <InvoicesPage {...props} />;
  if (["finance"].includes(props.tab)) return <FinancePage {...props} />;
  if (["staff"].includes(props.tab)) return <StaffPage {...props} />;
  if (["commissions"].includes(props.tab))
    return <CommissionsPage {...props} />;
  if (["rfm", "loyalty"].includes(props.tab)) return <RfmPage {...props} />;
  if (["campaigns"].includes(props.tab)) return <CampaignsPage {...props} />;
  if (["events"].includes(props.tab)) return <EventsPage {...props} />;
  if (["messages"].includes(props.tab)) return <MessagesPage {...props} />;
  if (["reports"].includes(props.tab)) return <ReportsPage {...props} />;
  if (["settings"].includes(props.tab)) return <SettingsPage {...props} />;
  if (["audit"].includes(props.tab)) return <AuditPage {...props} />;
  return null;
}
