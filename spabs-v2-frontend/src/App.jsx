import { Navigate, Route, BrowserRouter, Routes } from 'react-router-dom'
import AppShell from './components/layout/AppShell'
import ActivitiesListPage from './features/activities/ActivitiesListPage'
import ActivityDetailsPage from './features/activities/ActivityDetailsPage'
import ActivityFormPage from './features/activities/ActivityFormPage'
import RecurringActivityFormPage from './features/activities/RecurringActivityFormPage'
import { AuthProvider } from './features/auth/AuthContext'
import ChangePasswordPage from './features/auth/ChangePasswordPage'
import ForgotPasswordPage from './features/auth/ForgotPasswordPage'
import LoginPage from './features/auth/LoginPage'
import MyProfilePage from './features/auth/MyProfilePage'
import ResetPasswordPage from './features/auth/ResetPasswordPage'
import CoachPaymentFormPage from './features/finance/CoachPaymentFormPage'
import CoachPaymentsListPage from './features/finance/CoachPaymentsListPage'
import FeeItemFormPage from './features/finance/FeeItemFormPage'
import FeeItemsListPage from './features/finance/FeeItemsListPage'
import FeeRecordFormPage from './features/finance/FeeRecordFormPage'
import FeeRecordsListPage from './features/finance/FeeRecordsListPage'
import FinanceTransactionFormPage from './features/finance/FinanceTransactionFormPage'
import FinanceTransactionsListPage from './features/finance/FinanceTransactionsListPage'
import AnnouncementDetailsPage from './features/announcements/AnnouncementDetailsPage'
import AnnouncementFormPage from './features/announcements/AnnouncementFormPage'
import AnnouncementsListPage from './features/announcements/AnnouncementsListPage'
import InventoryDetailsPage from './features/inventory/InventoryDetailsPage'
import InventoryFormPage from './features/inventory/InventoryFormPage'
import InventoryListPage from './features/inventory/InventoryListPage'
import ClubSponsorshipFormPage from './features/sponsorship/ClubSponsorshipFormPage'
import ClubSponsorshipsListPage from './features/sponsorship/ClubSponsorshipsListPage'
import SponsorFormPage from './features/sponsorship/SponsorFormPage'
import SponsorsListPage from './features/sponsorship/SponsorsListPage'
import CoachActivitiesListPage from './features/coach-portal/CoachActivitiesListPage'
import CoachAnnouncementDetailsPage from './features/coach-portal/CoachAnnouncementDetailsPage'
import CoachAnnouncementsListPage from './features/coach-portal/CoachAnnouncementsListPage'
import CoachAttendancePage from './features/coach-portal/CoachAttendancePage'
import CoachInventoryPage from './features/coach-portal/CoachInventoryPage'
import CoachPayrollPage from './features/coach-portal/CoachPayrollPage'
import CoachPlayerDetailsPage from './features/coach-portal/CoachPlayerDetailsPage'
import CoachPlayerProgressPage from './features/coach-portal/CoachPlayerProgressPage'
import CoachTeamDetailsPage from './features/coach-portal/CoachTeamDetailsPage'
import CoachTeamsListPage from './features/coach-portal/CoachTeamsListPage'
import ParentActivitiesListPage from './features/parent-portal/ParentActivitiesListPage'
import ParentActivityDetailsPage from './features/parent-portal/ParentActivityDetailsPage'
import ParentAnnouncementDetailsPage from './features/parent-portal/ParentAnnouncementDetailsPage'
import ParentAnnouncementsListPage from './features/parent-portal/ParentAnnouncementsListPage'
import ParentChildAttendancePage from './features/parent-portal/ParentChildAttendancePage'
import ParentChildDetailsPage from './features/parent-portal/ParentChildDetailsPage'
import ParentChildFeesPage from './features/parent-portal/ParentChildFeesPage'
import ParentChildProgressPage from './features/parent-portal/ParentChildProgressPage'
import ParentFeesPage from './features/parent-portal/ParentFeesPage'
import ParentPlayersListPage from './features/parent-portal/ParentPlayersListPage'
import PlayerAttendanceHistoryPage from './features/players/PlayerAttendanceHistoryPage'
import PlayerDetailsPage from './features/players/PlayerDetailsPage'
import PlayerFormPage from './features/players/PlayerFormPage'
import PlayerProgressPage from './features/players/PlayerProgressPage'
import PlayersListPage from './features/players/PlayersListPage'
import ModuleFormPage from './features/progress/ModuleFormPage'
import SkillDetailsPage from './features/progress/SkillDetailsPage'
import SkillFormPage from './features/progress/SkillFormPage'
import SkillsListPage from './features/progress/SkillsListPage'
import TeamDetailsPage from './features/teams/TeamDetailsPage'
import TeamFormPage from './features/teams/TeamFormPage'
import TeamsListPage from './features/teams/TeamsListPage'
import UserDetailsPage from './features/users/UserDetailsPage'
import UserFormPage from './features/users/UserFormPage'
import UsersListPage from './features/users/UsersListPage'
import { ADMIN_ROLES, ROLES } from './lib/roles'
import ProtectedRoute from './routes/ProtectedRoute'
import RequireRole from './routes/RequireRole'
import AdminDashboard from './routes/dashboards/AdminDashboard'
import CoachDashboard from './routes/dashboards/CoachDashboard'
import ParentDashboard from './routes/dashboards/ParentDashboard'
import SuperAdminDashboard from './routes/dashboards/SuperAdminDashboard'

export default function App() {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route path="/forgot-password" element={<ForgotPasswordPage />} />
          <Route path="/reset-password" element={<ResetPasswordPage />} />

          <Route element={<ProtectedRoute />}>
            <Route path="/change-password" element={<ChangePasswordPage />} />

            <Route element={<AppShell />}>
              <Route path="/dashboard/super-admin" element={<SuperAdminDashboard />} />
              <Route path="/dashboard/admin" element={<AdminDashboard />} />
              <Route path="/dashboard/coach" element={<CoachDashboard />} />
              <Route path="/dashboard/parent" element={<ParentDashboard />} />

              <Route path="/profile" element={<MyProfilePage />} />

              <Route element={<RequireRole allowedRoles={ADMIN_ROLES} />}>
                <Route path="/users" element={<UsersListPage />} />
                <Route path="/users/new" element={<UserFormPage />} />
                <Route path="/users/:id" element={<UserDetailsPage />} />
                <Route path="/users/:id/edit" element={<UserFormPage />} />

                <Route path="/players" element={<PlayersListPage />} />
                <Route path="/players/new" element={<PlayerFormPage />} />
                <Route path="/players/:id" element={<PlayerDetailsPage />} />
                <Route path="/players/:id/edit" element={<PlayerFormPage />} />
                <Route path="/players/:id/attendance" element={<PlayerAttendanceHistoryPage />} />
                <Route path="/players/:id/progress" element={<PlayerProgressPage />} />

                <Route path="/teams" element={<TeamsListPage />} />
                <Route path="/teams/new" element={<TeamFormPage />} />
                <Route path="/teams/:id" element={<TeamDetailsPage />} />
                <Route path="/teams/:id/edit" element={<TeamFormPage />} />

                <Route path="/activities" element={<ActivitiesListPage />} />
                <Route path="/activities/new" element={<ActivityFormPage />} />
                <Route path="/activities/recurring" element={<RecurringActivityFormPage />} />
                <Route path="/activities/:id" element={<ActivityDetailsPage />} />
                <Route path="/activities/:id/edit" element={<ActivityFormPage />} />

                <Route path="/skills" element={<SkillsListPage />} />
                <Route path="/skills/new" element={<SkillFormPage />} />
                <Route path="/skills/:id" element={<SkillDetailsPage />} />
                <Route path="/skills/:id/edit" element={<SkillFormPage />} />
                <Route path="/skills/:skillId/modules/new" element={<ModuleFormPage />} />
                <Route path="/modules/:id/edit" element={<ModuleFormPage />} />

                <Route path="/fee-items" element={<FeeItemsListPage />} />
                <Route path="/fee-items/new" element={<FeeItemFormPage />} />
                <Route path="/fee-items/:id/edit" element={<FeeItemFormPage />} />
                <Route path="/fee-records" element={<FeeRecordsListPage />} />
                <Route path="/fee-records/new" element={<FeeRecordFormPage />} />

                <Route path="/inventory" element={<InventoryListPage />} />
                <Route path="/inventory/new" element={<InventoryFormPage />} />
                <Route path="/inventory/:id" element={<InventoryDetailsPage />} />
                <Route path="/inventory/:id/edit" element={<InventoryFormPage />} />

                <Route path="/sponsors" element={<SponsorsListPage />} />
                <Route path="/sponsors/new" element={<SponsorFormPage />} />
                <Route path="/sponsors/:id/edit" element={<SponsorFormPage />} />
                <Route path="/sponsorships" element={<ClubSponsorshipsListPage />} />
                <Route path="/sponsorships/new" element={<ClubSponsorshipFormPage />} />
                <Route path="/sponsorships/:id/edit" element={<ClubSponsorshipFormPage />} />

                <Route path="/announcements" element={<AnnouncementsListPage />} />
                <Route path="/announcements/new" element={<AnnouncementFormPage />} />
                <Route path="/announcements/:id" element={<AnnouncementDetailsPage />} />
                <Route path="/announcements/:id/edit" element={<AnnouncementFormPage />} />

                <Route path="/coach-payments" element={<CoachPaymentsListPage />} />
                <Route path="/coach-payments/new" element={<CoachPaymentFormPage />} />
                <Route path="/coach-payments/:id/edit" element={<CoachPaymentFormPage />} />
                <Route path="/finance-transactions" element={<FinanceTransactionsListPage />} />
                <Route path="/finance-transactions/new" element={<FinanceTransactionFormPage />} />
                <Route path="/finance-transactions/:id/edit" element={<FinanceTransactionFormPage />} />
              </Route>

              <Route element={<RequireRole allowedRoles={[ROLES.COACH]} />}>
                <Route path="/dashboard/coach/activities" element={<CoachActivitiesListPage />} />
                <Route path="/dashboard/coach/activities/:id" element={<CoachAttendancePage />} />
                <Route path="/dashboard/coach/teams" element={<CoachTeamsListPage />} />
                <Route path="/dashboard/coach/teams/:id" element={<CoachTeamDetailsPage />} />
                <Route
                  path="/dashboard/coach/teams/:teamId/players/:playerId"
                  element={<CoachPlayerDetailsPage />}
                />
                <Route
                  path="/dashboard/coach/teams/:teamId/players/:playerId/progress"
                  element={<CoachPlayerProgressPage />}
                />
                <Route path="/dashboard/coach/payroll" element={<CoachPayrollPage />} />
                <Route path="/dashboard/coach/inventory" element={<CoachInventoryPage />} />
                <Route path="/dashboard/coach/announcements" element={<CoachAnnouncementsListPage />} />
                <Route
                  path="/dashboard/coach/announcements/:id"
                  element={<CoachAnnouncementDetailsPage />}
                />
              </Route>

              <Route element={<RequireRole allowedRoles={[ROLES.PARENT]} />}>
                <Route path="/dashboard/parent/activities" element={<ParentActivitiesListPage />} />
                <Route path="/dashboard/parent/activities/:id" element={<ParentActivityDetailsPage />} />
                <Route path="/dashboard/parent/announcements" element={<ParentAnnouncementsListPage />} />
                <Route
                  path="/dashboard/parent/announcements/:id"
                  element={<ParentAnnouncementDetailsPage />}
                />
                <Route path="/dashboard/parent/players" element={<ParentPlayersListPage />} />
                <Route path="/dashboard/parent/fees" element={<ParentFeesPage />} />
                <Route path="/dashboard/parent/children/:id" element={<ParentChildDetailsPage />} />
                <Route path="/dashboard/parent/children/:id/fees" element={<ParentChildFeesPage />} />
                <Route
                  path="/dashboard/parent/children/:id/attendance"
                  element={<ParentChildAttendancePage />}
                />
                <Route
                  path="/dashboard/parent/children/:id/progress"
                  element={<ParentChildProgressPage />}
                />
              </Route>
            </Route>
          </Route>

          <Route path="/" element={<Navigate to="/login" replace />} />
          <Route path="*" element={<Navigate to="/login" replace />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  )
}
