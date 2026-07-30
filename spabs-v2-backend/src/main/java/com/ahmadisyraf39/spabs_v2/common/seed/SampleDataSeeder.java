package com.ahmadisyraf39.spabs_v2.common.seed;

import com.ahmadisyraf39.spabs_v2.activity.dto.request.ActivityRequest;
import com.ahmadisyraf39.spabs_v2.activity.dto.request.RecurringActivityRequest;
import com.ahmadisyraf39.spabs_v2.activity.dto.response.ActivityResponse;
import com.ahmadisyraf39.spabs_v2.activity.entity.enums.ActivityType;
import com.ahmadisyraf39.spabs_v2.activity.service.ActivityService;
import com.ahmadisyraf39.spabs_v2.announcement.dto.request.AnnouncementRequest;
import com.ahmadisyraf39.spabs_v2.announcement.service.AnnouncementService;
import com.ahmadisyraf39.spabs_v2.attendance.dto.request.BulkAttendanceEntry;
import com.ahmadisyraf39.spabs_v2.attendance.dto.request.BulkAttendanceRequest;
import com.ahmadisyraf39.spabs_v2.attendance.entity.enums.AttendanceStatus;
import com.ahmadisyraf39.spabs_v2.attendance.service.AttendanceService;
import com.ahmadisyraf39.spabs_v2.finance.dto.request.ActivityFinanceRequest;
import com.ahmadisyraf39.spabs_v2.finance.dto.request.CoachPaymentRequest;
import com.ahmadisyraf39.spabs_v2.finance.dto.request.FeeItemRequest;
import com.ahmadisyraf39.spabs_v2.finance.dto.request.FeeRecordRequest;
import com.ahmadisyraf39.spabs_v2.finance.dto.request.FinanceTransactionRequest;
import com.ahmadisyraf39.spabs_v2.finance.dto.response.CoachPaymentResponse;
import com.ahmadisyraf39.spabs_v2.finance.dto.response.FeeItemResponse;
import com.ahmadisyraf39.spabs_v2.finance.dto.response.FeeRecordResponse;
import com.ahmadisyraf39.spabs_v2.finance.entity.CoachPayment;
import com.ahmadisyraf39.spabs_v2.finance.entity.FeeRecord;
import com.ahmadisyraf39.spabs_v2.finance.entity.enums.CoachPaymentType;
import com.ahmadisyraf39.spabs_v2.finance.entity.enums.FeeType;
import com.ahmadisyraf39.spabs_v2.finance.entity.enums.FinanceCategory;
import com.ahmadisyraf39.spabs_v2.finance.entity.enums.FinanceReferenceType;
import com.ahmadisyraf39.spabs_v2.finance.entity.enums.FinanceType;
import com.ahmadisyraf39.spabs_v2.finance.entity.enums.PaymentMethod;
import com.ahmadisyraf39.spabs_v2.finance.entity.enums.PaymentStatus;
import com.ahmadisyraf39.spabs_v2.finance.repository.CoachPaymentRepository;
import com.ahmadisyraf39.spabs_v2.finance.repository.FeeRecordRepository;
import com.ahmadisyraf39.spabs_v2.finance.service.CoachPaymentService;
import com.ahmadisyraf39.spabs_v2.finance.service.FeeItemService;
import com.ahmadisyraf39.spabs_v2.finance.service.FeeRecordService;
import com.ahmadisyraf39.spabs_v2.finance.service.FinanceTransactionService;
import com.ahmadisyraf39.spabs_v2.inventory.dto.request.InventoryRequest;
import com.ahmadisyraf39.spabs_v2.inventory.dto.request.InventoryTransactionRequest;
import com.ahmadisyraf39.spabs_v2.inventory.dto.response.InventoryResponse;
import com.ahmadisyraf39.spabs_v2.inventory.entity.enums.InventoryCategory;
import com.ahmadisyraf39.spabs_v2.inventory.entity.enums.InventoryTransactionType;
import com.ahmadisyraf39.spabs_v2.inventory.service.InventoryService;
import com.ahmadisyraf39.spabs_v2.inventory.service.InventoryTransactionService;
import com.ahmadisyraf39.spabs_v2.membership.dto.request.CoachTeamRequest;
import com.ahmadisyraf39.spabs_v2.membership.dto.request.PlayerParentRequest;
import com.ahmadisyraf39.spabs_v2.membership.dto.request.PlayerTeamRequest;
import com.ahmadisyraf39.spabs_v2.membership.dto.response.PlayerTeamResponse;
import com.ahmadisyraf39.spabs_v2.membership.entity.enums.CoachTeamRole;
import com.ahmadisyraf39.spabs_v2.membership.entity.enums.CoachTeamStatus;
import com.ahmadisyraf39.spabs_v2.membership.entity.enums.ParentRelationship;
import com.ahmadisyraf39.spabs_v2.membership.entity.enums.PlayerTeamStatus;
import com.ahmadisyraf39.spabs_v2.membership.service.CoachTeamService;
import com.ahmadisyraf39.spabs_v2.membership.service.PlayerParentService;
import com.ahmadisyraf39.spabs_v2.membership.service.PlayerTeamService;
import com.ahmadisyraf39.spabs_v2.player.dto.request.PlayerRequest;
import com.ahmadisyraf39.spabs_v2.player.dto.response.PlayerResponse;
import com.ahmadisyraf39.spabs_v2.player.entity.enums.Gender;
import com.ahmadisyraf39.spabs_v2.player.service.PlayerService;
import com.ahmadisyraf39.spabs_v2.progress.dto.request.ModuleRequest;
import com.ahmadisyraf39.spabs_v2.progress.dto.request.PlayerModuleProgressRequest;
import com.ahmadisyraf39.spabs_v2.progress.dto.request.SkillRequest;
import com.ahmadisyraf39.spabs_v2.progress.dto.response.ModuleResponse;
import com.ahmadisyraf39.spabs_v2.progress.dto.response.SkillResponse;
import com.ahmadisyraf39.spabs_v2.progress.entity.enums.ModuleProgressStatus;
import com.ahmadisyraf39.spabs_v2.progress.service.ModuleService;
import com.ahmadisyraf39.spabs_v2.progress.service.PlayerModuleProgressService;
import com.ahmadisyraf39.spabs_v2.progress.service.SkillService;
import com.ahmadisyraf39.spabs_v2.security.UserPrincipal;
import com.ahmadisyraf39.spabs_v2.sponsorship.dto.request.ClubSponsorshipRequest;
import com.ahmadisyraf39.spabs_v2.sponsorship.dto.request.SponsorRequest;
import com.ahmadisyraf39.spabs_v2.sponsorship.dto.response.SponsorResponse;
import com.ahmadisyraf39.spabs_v2.sponsorship.entity.enums.SponsorshipType;
import com.ahmadisyraf39.spabs_v2.sponsorship.service.ClubSponsorshipService;
import com.ahmadisyraf39.spabs_v2.sponsorship.service.SponsorService;
import com.ahmadisyraf39.spabs_v2.team.dto.request.TeamRequest;
import com.ahmadisyraf39.spabs_v2.team.dto.response.TeamResponse;
import com.ahmadisyraf39.spabs_v2.team.entity.enums.AgeGroup;
import com.ahmadisyraf39.spabs_v2.team.entity.enums.TeamCategory;
import com.ahmadisyraf39.spabs_v2.team.repository.TeamRepository;
import com.ahmadisyraf39.spabs_v2.team.service.TeamService;
import com.ahmadisyraf39.spabs_v2.user.dto.request.AdminRequest;
import com.ahmadisyraf39.spabs_v2.user.dto.request.CoachRequest;
import com.ahmadisyraf39.spabs_v2.user.dto.request.ParentRequest;
import com.ahmadisyraf39.spabs_v2.user.dto.request.UserRequest;
import com.ahmadisyraf39.spabs_v2.user.dto.response.CoachResponse;
import com.ahmadisyraf39.spabs_v2.user.dto.response.ParentResponse;
import com.ahmadisyraf39.spabs_v2.user.dto.response.UserResponse;
import com.ahmadisyraf39.spabs_v2.user.entity.User;
import com.ahmadisyraf39.spabs_v2.user.entity.enums.CoachCertification;
import com.ahmadisyraf39.spabs_v2.user.entity.enums.CoachSpecialization;
import com.ahmadisyraf39.spabs_v2.user.entity.enums.UserRole;
import com.ahmadisyraf39.spabs_v2.user.repository.UserRepository;
import com.ahmadisyraf39.spabs_v2.user.service.AdminService;
import com.ahmadisyraf39.spabs_v2.user.service.CoachService;
import com.ahmadisyraf39.spabs_v2.user.service.ParentService;
import com.ahmadisyraf39.spabs_v2.user.service.UserService;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SampleDataSeeder implements CommandLineRunner {

    private static final String SAMPLE_PASSWORD = "Password123";
    private static final LocalDate HISTORY_START = LocalDate.of(2025, 1, 6);

    private static final String[] MALE_FIRST_NAMES = {
        "Ali", "Danish", "Farid", "Amir", "Haziq", "Zul", "Naufal", "Aiman", "Iskandar", "Faiz",
        "Rayyan", "Adam", "Hakim", "Zaid", "Irfan", "Amsyar", "Danial", "Luqman", "Syafiq", "Hafiz"
    };
    private static final String[] FEMALE_FIRST_NAMES = {
        "Aisyah", "Nur", "Sofea", "Batrisyia", "Alya", "Qistina", "Balqis", "Iman", "Farah", "Aina",
        "Sarah", "Nadia", "Amira", "Husna", "Adriana", "Maisarah", "Damia", "Zara", "Elina", "Wafiya"
    };
    private static final String[] LAST_NAMES = {
        "Hassan", "Rahman", "Ibrahim", "Kamal", "Yusof", "Zulkifli", "Osman", "Abdullah", "Rashid",
        "Aziz", "Malik", "Karim", "Salleh", "Idris", "Bakar", "Latif", "Hamid", "Nordin", "Shah", "Rosli"
    };
    private static final String[] OPPONENT_CLUBS = {
        "Lions FC", "Falcons FC", "Panthers FC", "Warriors FC", "Rangers FC", "Comets FC"
    };

    private static final List<TeamSpec> TEAM_SPECS = List.of(
            new TeamSpec("Tigers U10 Boys", AgeGroup.U10, TeamCategory.BOYS, 12, Gender.MALE, DayOfWeek.MONDAY, "Field A"),
            new TeamSpec("Tigers U12 Boys", AgeGroup.U12, TeamCategory.BOYS, 14, Gender.MALE, DayOfWeek.TUESDAY, "Field A"),
            new TeamSpec("Lions U14 Boys", AgeGroup.U14, TeamCategory.BOYS, 14, Gender.MALE, DayOfWeek.WEDNESDAY, "Field B"),
            new TeamSpec("Lions U16 Boys", AgeGroup.U16, TeamCategory.BOYS, 12, Gender.MALE, DayOfWeek.THURSDAY, "Field B"),
            new TeamSpec("Eagles U12 Girls", AgeGroup.U12, TeamCategory.GIRLS, 14, Gender.FEMALE, DayOfWeek.FRIDAY, "Field C"),
            new TeamSpec("Eagles U14 Girls", AgeGroup.U14, TeamCategory.GIRLS, 14, Gender.FEMALE, DayOfWeek.SATURDAY, "Field C"));

    private final Random random = new Random(42);

    private final Set<String> usedFullNames = new HashSet<>();

    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final FeeRecordRepository feeRecordRepository;
    private final CoachPaymentRepository coachPaymentRepository;
    private final UserService userService;
    private final AdminService adminService;
    private final CoachService coachService;
    private final ParentService parentService;
    private final PlayerService playerService;
    private final TeamService teamService;
    private final PlayerTeamService playerTeamService;
    private final CoachTeamService coachTeamService;
    private final PlayerParentService playerParentService;
    private final SkillService skillService;
    private final ModuleService moduleService;
    private final PlayerModuleProgressService playerModuleProgressService;
    private final ActivityService activityService;
    private final AttendanceService attendanceService;
    private final FeeItemService feeItemService;
    private final FeeRecordService feeRecordService;
    private final CoachPaymentService coachPaymentService;
    private final FinanceTransactionService financeTransactionService;
    private final InventoryService inventoryService;
    private final InventoryTransactionService inventoryTransactionService;
    private final SponsorService sponsorService;
    private final ClubSponsorshipService clubSponsorshipService;
    private final AnnouncementService announcementService;

    @Value("${app.seed.sample-data.enabled}")
    private boolean enabled;

    private record TeamSpec(
            String name,
            AgeGroup ageGroup,
            TeamCategory category,
            int playerCount,
            Gender gender,
            DayOfWeek trainingDay,
            String location) {}

    private record RosterPlayer(PlayerResponse player, LocalDate joinedAt, Long playerTeamId) {}

    private record TeamRoster(TeamResponse team, UserPrincipal coachUser, CoachResponse coach, List<RosterPlayer> players) {}

    private record CoachPayrollEntry(CoachResponse coach, CoachPaymentType type, BigDecimal amount, LocalDate startDate) {}

    @Override
    public void run(String... args) {
        if (!enabled || teamRepository.count() > 0) {
            return;
        }
        LocalDate today = LocalDate.now();

        UserPrincipal admin = createPerson("admin.sample@spabs.example", "Sample Admin", UserRole.ADMIN);
        adminService.create(AdminRequest.builder().userId(admin.getId()).build());
        UserPrincipal admin2 = createPerson("admin.two@spabs.example", "Second Admin", UserRole.ADMIN);
        adminService.create(AdminRequest.builder().userId(admin2.getId()).build());

        List<CoachPayrollEntry> payroll = new ArrayList<>();
        List<TeamRoster> rosters = seedTeamsAndRosters(today, payroll);
        seedTeamTransfers(rosters, today);
        seedParents(rosters);
        seedSkillsAndProgress(rosters);
        Map<Long, ActivityResponse> tournamentsByTeam = seedActivitiesAndAttendance(rosters, admin, today);
        seedFees(rosters, admin, today);
        seedPayroll(payroll, today);
        seedTournamentFinance(tournamentsByTeam, today);
        seedRecurringExpenses(today);
        seedInventory();
        seedSponsorship();
        seedAnnouncements(rosters, admin);

        log.info(
                "Seeded sample data: {} teams, {} coaches, {} players. Every account uses password"
                        + " '{}'. Admins: admin.sample@spabs.example, admin.two@spabs.example. Coaches:"
                        + " coach1@spabs.example .. coach{}@spabs.example, plus coach.assist1@spabs.example"
                        + " and coach.assist2@spabs.example. Parent emails are printed per-player in the"
                        + " database (players table) — look up any player's parent via"
                        + " GET /api/v1/player-parents?playerId=.",
                rosters.size(),
                payroll.size(),
                rosters.stream().mapToInt(r -> r.players().size()).sum(),
                SAMPLE_PASSWORD,
                rosters.size());
    }

    private List<TeamRoster> seedTeamsAndRosters(LocalDate today, List<CoachPayrollEntry> payroll) {
        List<TeamRoster> rosters = new ArrayList<>();
        int playerGlobalIndex = 0;
        for (int i = 0; i < TEAM_SPECS.size(); i++) {
            TeamSpec spec = TEAM_SPECS.get(i);
            TeamResponse team = teamService.create(TeamRequest.builder()
                    .name(spec.name())
                    .category(spec.category())
                    .ageGroup(spec.ageGroup())
                    .build());

            UserPrincipal coachUser = createPerson(
                    "coach" + (i + 1) + "@spabs.example", generateName(300 + i, i % 2 == 0 ? Gender.MALE : Gender.FEMALE), UserRole.COACH);
            CoachResponse coach = coachService.create(CoachRequest.builder()
                    .userId(coachUser.getId())
                    .specialization(CoachSpecialization.values()[i % CoachSpecialization.values().length])
                    .certification(CoachCertification.values()[i % CoachCertification.values().length])
                    .build());
            coachTeamService.create(CoachTeamRequest.builder()
                    .coachId(coach.getId())
                    .teamId(team.getId())
                    .role(CoachTeamRole.HEAD_COACH)
                    .status(CoachTeamStatus.ACTIVE)
                    .joinedAt(HISTORY_START.minusDays(5))
                    .build());
            payroll.add(new CoachPayrollEntry(coach, CoachPaymentType.SALARY, new BigDecimal("800.00"), HISTORY_START.minusDays(5)));

            List<RosterPlayer> players = new ArrayList<>();
            for (int p = 0; p < spec.playerCount(); p++) {
                LocalDate joinedAt = randomJoinDate(today);
                PlayerResponse player = createPlayer(playerGlobalIndex, spec.gender(), spec.ageGroup(), today);
                PlayerTeamResponse playerTeam = playerTeamService.create(PlayerTeamRequest.builder()
                        .playerId(player.getId())
                        .teamId(team.getId())
                        .status(PlayerTeamStatus.ACTIVE)
                        .joinedAt(joinedAt)
                        .build());
                players.add(new RosterPlayer(player, joinedAt, playerTeam.getId()));
                playerGlobalIndex++;
            }
            rosters.add(new TeamRoster(team, coachUser, coach, players));
        }

        addAssistantCoach(rosters.get(2), "coach.assist1@spabs.example", 320, payroll);
        addAssistantCoach(rosters.get(5), "coach.assist2@spabs.example", 321, payroll);
        return rosters;
    }

    private void seedTeamTransfers(List<TeamRoster> rosters, LocalDate today) {
        int[][] progressionPairs = {{0, 1}, {1, 2}, {2, 3}, {4, 5}};
        for (int[] pair : progressionPairs) {
            TeamRoster from = rosters.get(pair[0]);
            TeamRoster to = rosters.get(pair[1]);
            for (int i = 0; i < from.players().size(); i++) {
                if (i % 4 != 0) {
                    continue;
                }
                transferPlayer(from, to, from.players().get(i), today);
            }
        }
    }

    private void transferPlayer(TeamRoster from, TeamRoster to, RosterPlayer rp, LocalDate today) {
        LocalDate latestAllowed = today.minusDays(1);
        LocalDate leftAt = rp.joinedAt().plusMonths(10 + random.nextInt(5));
        if (leftAt.isAfter(latestAllowed)) {
            leftAt = latestAllowed;
        }
        LocalDate newJoinedAt = leftAt.plusDays(7 + random.nextInt(21));
        if (newJoinedAt.isAfter(latestAllowed)) {
            newJoinedAt = latestAllowed;
        }
        if (!newJoinedAt.isAfter(leftAt)) {
            return;
        }

        playerTeamService.update(
                rp.playerTeamId(),
                PlayerTeamRequest.builder()
                        .playerId(rp.player().getId())
                        .teamId(from.team().getId())
                        .status(PlayerTeamStatus.INACTIVE)
                        .joinedAt(rp.joinedAt())
                        .leftAt(leftAt)
                        .build());
        playerTeamService.create(PlayerTeamRequest.builder()
                .playerId(rp.player().getId())
                .teamId(to.team().getId())
                .status(PlayerTeamStatus.ACTIVE)
                .joinedAt(newJoinedAt)
                .build());
    }

    private void addAssistantCoach(TeamRoster roster, String email, int nameIndex, List<CoachPayrollEntry> payroll) {
        UserPrincipal assistantUser = createPerson(email, generateName(nameIndex, Gender.MALE), UserRole.COACH);
        CoachResponse assistant = coachService.create(CoachRequest.builder()
                .userId(assistantUser.getId())
                .specialization(CoachSpecialization.FITNESS)
                .certification(CoachCertification.FAM_C)
                .build());
        coachTeamService.create(CoachTeamRequest.builder()
                .coachId(assistant.getId())
                .teamId(roster.team().getId())
                .role(CoachTeamRole.ASSISTANT_COACH)
                .status(CoachTeamStatus.ACTIVE)
                .joinedAt(HISTORY_START.plusMonths(2))
                .build());
        payroll.add(new CoachPayrollEntry(
                assistant, CoachPaymentType.PER_SESSION, new BigDecimal("300.00"), HISTORY_START.plusMonths(2)));
    }

    private PlayerResponse createPlayer(int globalIndex, Gender gender, AgeGroup ageGroup, LocalDate today) {
        String name = generateName(globalIndex, gender);
        int targetAge = targetAgeForBucket(ageGroup);
        LocalDate birthYearStart = today.minusYears(targetAge).withDayOfYear(1);
        LocalDate dob = birthYearStart.plusDays(random.nextInt(365));
        return playerService.create(
                PlayerRequest.builder().fullName(name).dateOfBirth(dob).gender(gender).build());
    }

    private int targetAgeForBucket(AgeGroup ageGroup) {
        return switch (ageGroup) {
            case U8 -> 7;
            case U10 -> 9;
            case U12 -> 11;
            case U14 -> 13;
            case U16 -> 15;
            case U18 -> 17;
            case OPEN -> 20;
        };
    }

    private String generateName(int index, Gender gender) {
        String[] firstNames = gender == Gender.MALE ? MALE_FIRST_NAMES : FEMALE_FIRST_NAMES;
        int poolSize = firstNames.length * LAST_NAMES.length;
        for (int attempt = 0; attempt < poolSize; attempt++) {
            int i = Math.floorMod(index + attempt, poolSize);
            String candidate = nameAt(i, firstNames);
            if (usedFullNames.add(candidate)) {
                return candidate;
            }
        }
        String fallback = nameAt(Math.floorMod(index, poolSize), firstNames) + " " + index;
        usedFullNames.add(fallback);
        return fallback;
    }

    private String nameAt(int i, String[] firstNames) {
        String first = firstNames[i % firstNames.length];
        String last = LAST_NAMES[(i / firstNames.length) % LAST_NAMES.length];
        return first + " " + last;
    }

    private LocalDate randomJoinDate(LocalDate today) {
        LocalDate foundingWindowEnd = HISTORY_START.plusMonths(2);
        LocalDate lateWindowEnd = today.minusMonths(2);
        if (random.nextInt(100) < 60 || lateWindowEnd.isBefore(foundingWindowEnd)) {
            return randomDateBetween(HISTORY_START, foundingWindowEnd);
        }
        return randomDateBetween(foundingWindowEnd, lateWindowEnd);
    }

    private LocalDate randomDateBetween(LocalDate startInclusive, LocalDate endExclusive) {
        long days = ChronoUnit.DAYS.between(startInclusive, endExclusive);
        if (days <= 0) {
            return startInclusive;
        }
        return startInclusive.plusDays(random.nextInt((int) days));
    }

    private void seedParents(List<TeamRoster> rosters) {
        int parentNameIndex = 200;
        int playerCounter = 0;
        ParentResponse previousParent = null;
        Gender previousParentGender = null;
        for (TeamRoster roster : rosters) {
            for (RosterPlayer rp : roster.players()) {
                if (playerCounter % 4 == 3 && previousParent != null) {
                    playerParentService.create(PlayerParentRequest.builder()
                            .playerId(rp.player().getId())
                            .parentId(previousParent.getId())
                            .relationship(randomRelationship(previousParentGender))
                            .build());
                } else {
                    Gender parentGender = random.nextBoolean() ? Gender.MALE : Gender.FEMALE;
                    String name = generateName(parentNameIndex++, parentGender);
                    String email = "parent" + playerCounter + "@spabs.example";
                    UserPrincipal parentUser = createPerson(email, name, UserRole.PARENT);
                    ParentResponse parent = parentService.create(ParentRequest.builder()
                            .userId(parentUser.getId())
                            .emergencyContact("012-" + (1000000 + random.nextInt(8999999)))
                            .address("No. " + (1 + random.nextInt(200)) + ", Jalan Sukan, Kuala Lumpur")
                            .build());
                    playerParentService.create(PlayerParentRequest.builder()
                            .playerId(rp.player().getId())
                            .parentId(parent.getId())
                            .relationship(randomRelationship(parentGender))
                            .build());
                    previousParent = parent;
                    previousParentGender = parentGender;
                }
                playerCounter++;
            }
        }
    }

    private ParentRelationship randomRelationship(Gender parentGender) {
        if (random.nextInt(100) < 15) {
            return ParentRelationship.GUARDIAN;
        }
        return parentGender == Gender.MALE ? ParentRelationship.FATHER : ParentRelationship.MOTHER;
    }

    private void seedSkillsAndProgress(List<TeamRoster> rosters) {
        for (int i = 0; i < rosters.size(); i++) {
            TeamRoster roster = rosters.get(i);
            TeamSpec spec = TEAM_SPECS.get(i);

            SkillResponse ballControl = skillService.create(SkillRequest.builder()
                    .ageGroup(spec.ageGroup())
                    .category(spec.category())
                    .name("Ball Control")
                    .description("First touch and close control")
                    .build());
            SkillResponse passing = skillService.create(SkillRequest.builder()
                    .ageGroup(spec.ageGroup())
                    .category(spec.category())
                    .name("Passing & Vision")
                    .description("Passing accuracy and field awareness")
                    .build());
            List<ModuleResponse> modules = List.of(
                    createModule(ballControl.getId(), "First touch"),
                    createModule(ballControl.getId(), "Close dribbling"),
                    createModule(passing.getId(), "Short passing"),
                    createModule(passing.getId(), "Scanning the field"));

            for (RosterPlayer rp : roster.players()) {
                if (random.nextInt(100) >= 70) {
                    continue;
                }
                for (ModuleResponse module : modules) {
                    if (random.nextInt(100) < 60) {
                        recordProgress(
                                module.getId(), rp.player().getId(), roster.team().getId(), randomProgressStatus(), roster.coachUser());
                    }
                }
            }
        }
    }

    private ModuleResponse createModule(Long skillId, String name) {
        return moduleService.create(ModuleRequest.builder()
                .skillId(skillId)
                .name(name)
                .criteria25("Introduced")
                .criteria50("Consistent under no pressure")
                .criteria75("Consistent under light pressure")
                .criteria100("Match-ready")
                .build());
    }

    private void recordProgress(Long moduleId, Long playerId, Long teamId, ModuleProgressStatus status, UserPrincipal coach) {
        playerModuleProgressService.save(
                PlayerModuleProgressRequest.builder()
                        .moduleId(moduleId)
                        .playerId(playerId)
                        .teamId(teamId)
                        .status(status)
                        .build(),
                coach);
    }

    private ModuleProgressStatus randomProgressStatus() {
        int r = random.nextInt(100);
        if (r < 10) return ModuleProgressStatus.NOT_STARTED;
        if (r < 30) return ModuleProgressStatus.STARTED;
        if (r < 60) return ModuleProgressStatus.IN_PROGRESS;
        if (r < 85) return ModuleProgressStatus.ALMOST_COMPLETE;
        return ModuleProgressStatus.COMPLETED;
    }

    private Map<Long, ActivityResponse> seedActivitiesAndAttendance(List<TeamRoster> rosters, UserPrincipal admin, LocalDate today) {
        Map<Long, ActivityResponse> tournamentsByTeam = new LinkedHashMap<>();
        LocalDate trainingEnd = today.plusWeeks(3);

        for (int i = 0; i < rosters.size(); i++) {
            TeamRoster roster = rosters.get(i);
            TeamSpec spec = TEAM_SPECS.get(i);

            List<ActivityResponse> trainings = activityService.createRecurring(RecurringActivityRequest.builder()
                    .teamIds(List.of(roster.team().getId()))
                    .type(ActivityType.TRAINING)
                    .title("Weekly Training")
                    .location(spec.location())
                    .dayOfWeek(spec.trainingDay())
                    .startTime(LocalTime.of(17, 0))
                    .endTime(LocalTime.of(18, 30))
                    .startDate(HISTORY_START)
                    .endDate(trainingEnd)
                    .build());
            for (ActivityResponse training : trainings) {
                maybeRecordAttendance(training, roster, admin, today);
            }

            YearMonth matchStart = YearMonth.from(HISTORY_START).plusMonths(1);
            YearMonth matchEnd = YearMonth.from(today);
            for (YearMonth ym = matchStart; !ym.isAfter(matchEnd); ym = ym.plusMonths(1)) {
                LocalDate matchDate = thirdSaturday(ym);
                ActivityResponse match = activityService.create(ActivityRequest.builder()
                        .teamId(roster.team().getId())
                        .type(ActivityType.MATCH)
                        .title("Friendly vs " + OPPONENT_CLUBS[Math.floorMod(i + ym.getMonthValue(), OPPONENT_CLUBS.length)])
                        .startAt(matchDate.atTime(9, 0))
                        .location("Central Stadium")
                        .build());
                maybeRecordAttendance(match, roster, admin, today);
            }

            LocalDate tournamentStart =
                    i == rosters.size() - 1 ? today.plusWeeks(2) : LocalDate.of(2025, 4, 15).plusMonths(3L * i);
            ActivityResponse tournament = activityService.create(ActivityRequest.builder()
                    .teamId(roster.team().getId())
                    .type(ActivityType.TOURNAMENT)
                    .title(spec.name() + " Invitational " + tournamentStart.getYear())
                    .startAt(tournamentStart.atTime(9, 0))
                    .endAt(tournamentStart.plusDays(2).atTime(18, 0))
                    .location("State Stadium")
                    .build());
            tournamentsByTeam.put(roster.team().getId(), tournament);
        }
        return tournamentsByTeam;
    }

    private void maybeRecordAttendance(ActivityResponse activity, TeamRoster roster, UserPrincipal admin, LocalDate today) {
        LocalDate activityDate = activity.getStartAt().toLocalDate();
        if (!activityDate.isBefore(today)) {
            return;
        }
        if (random.nextInt(100) < 15) {
            return;
        }
        List<BulkAttendanceEntry> entries = roster.players().stream()
                .filter(rp -> !rp.joinedAt().isAfter(activityDate))
                .map(rp -> BulkAttendanceEntry.builder()
                        .playerId(rp.player().getId())
                        .status(randomAttendanceStatus())
                        .build())
                .toList();
        if (entries.isEmpty()) {
            return;
        }
        attendanceService.saveBulk(
                BulkAttendanceRequest.builder().activityId(activity.getId()).entries(entries).build(), admin);
    }

    private AttendanceStatus randomAttendanceStatus() {
        int r = random.nextInt(100);
        if (r < 78) return AttendanceStatus.PRESENT;
        if (r < 88) return AttendanceStatus.ABSENT;
        if (r < 96) return AttendanceStatus.LATE;
        return AttendanceStatus.EXCUSED;
    }

    private LocalDate thirdSaturday(YearMonth yearMonth) {
        LocalDate date = yearMonth.atDay(1);
        int count = 0;
        while (true) {
            if (date.getDayOfWeek() == DayOfWeek.SATURDAY) {
                count++;
                if (count == 3) {
                    return date;
                }
            }
            date = date.plusDays(1);
        }
    }

    private void seedFees(List<TeamRoster> rosters, UserPrincipal admin, LocalDate today) {
        FeeItemResponse registrationFee = feeItemService.create(FeeItemRequest.builder()
                .name("Registration Fee")
                .feeType(FeeType.REGISTRATION)
                .amount(new BigDecimal("250.00"))
                .build());
        FeeItemResponse monthlyFee = feeItemService.create(FeeItemRequest.builder()
                .name("Monthly Training Fee")
                .feeType(FeeType.MONTHLY)
                .amount(new BigDecimal("120.00"))
                .build());

        for (TeamRoster roster : rosters) {
            for (RosterPlayer rp : roster.players()) {
                FeeRecordResponse registration = feeRecordService.assignFee(
                        FeeRecordRequest.builder()
                                .playerId(rp.player().getId())
                                .feeItemId(registrationFee.getId())
                                .teamId(roster.team().getId())
                                .dueDate(rp.joinedAt())
                                .build(),
                        admin);
                if (random.nextInt(100) < 92) {
                    markFeePaid(registration, rp.joinedAt().plusDays(random.nextInt(7)));
                }

                YearMonth start = YearMonth.from(rp.joinedAt()).plusMonths(1);
                YearMonth end = YearMonth.from(today);
                for (YearMonth ym = start; !ym.isAfter(end); ym = ym.plusMonths(1)) {
                    LocalDate dueDate = ym.atDay(5);
                    FeeRecordResponse fee = feeRecordService.assignFee(
                            FeeRecordRequest.builder()
                                    .playerId(rp.player().getId())
                                    .feeItemId(monthlyFee.getId())
                                    .teamId(roster.team().getId())
                                    .dueDate(dueDate)
                                    .build(),
                            admin);
                    if (dueDate.isBefore(today) && random.nextInt(100) < 88) {
                        LocalDate paidAt = dueDate.plusDays(random.nextInt(10));
                        markFeePaid(fee, paidAt.isAfter(today) ? today : paidAt);
                    }
                }
            }
        }
    }

    private void markFeePaid(FeeRecordResponse feeRecord, LocalDate paidDate) {
        FeeRecord entity = feeRecordRepository.findById(feeRecord.getId()).orElseThrow();
        entity.setStatus(PaymentStatus.PAID);
        entity.setPaidAt(paidDate);
        entity.setPaymentMethod(PaymentMethod.MANUAL);
        FeeRecord saved = feeRecordRepository.save(entity);
        financeTransactionService.recordLinked(
                FinanceType.INCOME,
                feeCategory(saved.getFeeItem().getFeeType()),
                saved.getAmount(),
                saved.getFeeItem().getName() + " - " + saved.getPlayer().getFullName(),
                paidDate,
                saved.getId(),
                FinanceReferenceType.FEE_RECORD);
    }

    private FinanceCategory feeCategory(FeeType feeType) {
        return switch (feeType) {
            case REGISTRATION -> FinanceCategory.REGISTRATION_FEE;
            case MONTHLY -> FinanceCategory.MONTHLY_FEE;
            case APPAREL, OTHER -> FinanceCategory.OTHER;
        };
    }

    private void seedPayroll(List<CoachPayrollEntry> payroll, LocalDate today) {
        for (CoachPayrollEntry entry : payroll) {
            YearMonth start = YearMonth.from(entry.startDate());
            YearMonth end = YearMonth.from(today);
            for (YearMonth ym = start; !ym.isAfter(end); ym = ym.plusMonths(1)) {
                LocalDate paymentDate = ym.atDay(28);
                if (paymentDate.isAfter(today)) {
                    paymentDate = today;
                }
                CoachPaymentResponse payment = coachPaymentService.create(CoachPaymentRequest.builder()
                        .coachId(entry.coach().getId())
                        .amount(entry.amount())
                        .paymentDate(paymentDate)
                        .paymentType(entry.type())
                        .remarks(ym.getMonth() + " " + ym.getYear())
                        .build());
                if (!ym.equals(end)) {
                    markCoachPaymentPaid(payment, paymentDate);
                }
            }
        }
    }

    private void markCoachPaymentPaid(CoachPaymentResponse response, LocalDate paidDate) {
        CoachPayment entity = coachPaymentRepository.findById(response.getId()).orElseThrow();
        entity.setStatus(PaymentStatus.PAID);
        CoachPayment saved = coachPaymentRepository.save(entity);
        financeTransactionService.recordLinked(
                FinanceType.EXPENSE,
                coachPaymentCategory(saved.getPaymentType()),
                saved.getAmount(),
                "Coach payment - " + saved.getCoach().getUser().getFullName(),
                paidDate,
                saved.getId(),
                FinanceReferenceType.COACH_PAYMENT);
    }

    private FinanceCategory coachPaymentCategory(CoachPaymentType type) {
        return switch (type) {
            case SALARY -> FinanceCategory.COACH_SALARY;
            case PER_SESSION -> FinanceCategory.COACH_PER_SESSION;
            case BONUS -> FinanceCategory.COACH_BONUS;
        };
    }

    private void seedTournamentFinance(Map<Long, ActivityResponse> tournamentsByTeam, LocalDate today) {
        for (ActivityResponse tournament : tournamentsByTeam.values()) {
            financeTransactionService.recordActivityFinanceEntry(ActivityFinanceRequest.builder()
                    .activityId(tournament.getId())
                    .financeType(FinanceType.EXPENSE)
                    .financeCategory(FinanceCategory.TOURNAMENT_REGISTRATION)
                    .amount(new BigDecimal("150.00"))
                    .description("Tournament entry fee")
                    .transactionDate(tournament.getStartAt().toLocalDate().minusDays(10))
                    .build());
            boolean alreadyHappened = tournament.getEndAt() != null && tournament.getEndAt().toLocalDate().isBefore(today);
            if (alreadyHappened && random.nextInt(100) < 40) {
                financeTransactionService.recordActivityFinanceEntry(ActivityFinanceRequest.builder()
                        .activityId(tournament.getId())
                        .financeType(FinanceType.INCOME)
                        .financeCategory(FinanceCategory.PRIZE_MONEY)
                        .amount(new BigDecimal(200 + random.nextInt(600)))
                        .description("Tournament prize")
                        .transactionDate(tournament.getEndAt().toLocalDate())
                        .build());
            }
        }
    }

    private void seedRecurringExpenses(LocalDate today) {
        YearMonth start = YearMonth.from(HISTORY_START);
        YearMonth end = YearMonth.from(today);
        for (YearMonth ym = start; !ym.isAfter(end); ym = ym.plusMonths(1)) {
            financeTransactionService.create(FinanceTransactionRequest.builder()
                    .financeType(FinanceType.EXPENSE)
                    .financeCategory(FinanceCategory.FIELD_RENTAL)
                    .amount(new BigDecimal("300.00"))
                    .description("Monthly field rental - " + ym)
                    .transactionDate(ym.atDay(1))
                    .build());
            if (random.nextInt(100) < 50) {
                financeTransactionService.create(FinanceTransactionRequest.builder()
                        .financeType(FinanceType.EXPENSE)
                        .financeCategory(FinanceCategory.REFEREE)
                        .amount(new BigDecimal(60 + random.nextInt(60)))
                        .description("Referee fees - " + ym)
                        .transactionDate(ym.atDay(20))
                        .build());
            }
        }
    }

    private void seedInventory() {
        seedInventoryItem("Footballs (Size 5)", InventoryCategory.BALL, "Match and training balls", 20, new BigDecimal("45.00"));
        seedInventoryItem("Footballs (Size 4)", InventoryCategory.BALL, "Youth training balls", 15, new BigDecimal("40.00"));
        seedInventoryItem("Training Bibs", InventoryCategory.BIB, "Set of coloured bibs", 40, new BigDecimal("8.00"));
        seedInventoryItem("Marking Cones", InventoryCategory.CONE, "Agility and marking cones", 60, new BigDecimal("2.50"));
        seedInventoryItem(
                "Agility Ladders", InventoryCategory.AGILITY_LADDER, "Speed and agility training ladders", 8, new BigDecimal("60.00"));
        seedInventoryItem("First Aid Kits", InventoryCategory.OTHER, "Pitch-side first aid kits", 6, new BigDecimal("35.00"));
    }

    private void seedInventoryItem(
            String name, InventoryCategory category, String description, int initialQty, BigDecimal unitPrice) {
        InventoryResponse item = inventoryService.create(InventoryRequest.builder()
                .name(name)
                .category(category)
                .description(description)
                .currentQuantity(0)
                .build());
        inventoryTransactionService.recordTransaction(InventoryTransactionRequest.builder()
                .inventoryId(item.getId())
                .transactionType(InventoryTransactionType.INITIAL_STOCK)
                .quantity(initialQty)
                .transactionDate(HISTORY_START.plusDays(4))
                .build());
        int restockQty = Math.max(2, initialQty / 3);
        inventoryTransactionService.recordTransaction(InventoryTransactionRequest.builder()
                .inventoryId(item.getId())
                .transactionType(InventoryTransactionType.PURCHASE)
                .quantity(restockQty)
                .transactionDate(HISTORY_START.plusMonths(9))
                .price(unitPrice.multiply(BigDecimal.valueOf(restockQty)))
                .remarks("Restock")
                .build());
        if (random.nextInt(100) < 40) {
            inventoryTransactionService.recordTransaction(InventoryTransactionRequest.builder()
                    .inventoryId(item.getId())
                    .transactionType(InventoryTransactionType.DAMAGE)
                    .quantity(1 + random.nextInt(3))
                    .transactionDate(HISTORY_START.plusMonths(14))
                    .remarks("Wear and tear")
                    .build());
        }
    }

    private void seedSponsorship() {
        SponsorResponse sponsor1 = sponsorService.create(SponsorRequest.builder()
                .name("Local Sports Store")
                .contactPerson("Mr. Tan")
                .phoneNumber("012-3456789")
                .email("contact@localsportsstore.example")
                .address("123 Jalan Sukan")
                .build());
        clubSponsorshipService.create(ClubSponsorshipRequest.builder()
                .sponsorId(sponsor1.getId())
                .sponsorshipType(SponsorshipType.EQUIPMENT)
                .amount(new BigDecimal("1500.00"))
                .description("2025 season equipment sponsorship")
                .startDate(LocalDate.of(2025, 1, 1))
                .endDate(LocalDate.of(2025, 12, 31))
                .build());

        SponsorResponse sponsor2 = sponsorService.create(SponsorRequest.builder()
                .name("City Cafe & Restaurant")
                .contactPerson("Ms. Lee")
                .phoneNumber("013-9876543")
                .email("info@citycafe.example")
                .address("45 Jalan Bandar")
                .build());
        clubSponsorshipService.create(ClubSponsorshipRequest.builder()
                .sponsorId(sponsor2.getId())
                .sponsorshipType(SponsorshipType.CASH)
                .amount(new BigDecimal("3000.00"))
                .description("2026 season main sponsorship")
                .startDate(LocalDate.of(2026, 1, 1))
                .endDate(LocalDate.of(2026, 12, 31))
                .build());

        SponsorResponse sponsor3 = sponsorService.create(SponsorRequest.builder()
                .name("SportsGear Malaysia")
                .contactPerson("Mr. Kumar")
                .phoneNumber("017-2223334")
                .email("partnerships@sportsgear.example")
                .address("8 Jalan Perindustrian")
                .build());
        clubSponsorshipService.create(ClubSponsorshipRequest.builder()
                .sponsorId(sponsor3.getId())
                .sponsorshipType(SponsorshipType.JERSEY)
                .amount(new BigDecimal("2200.00"))
                .description("Match-day jersey sponsorship")
                .startDate(LocalDate.of(2026, 3, 1))
                .endDate(LocalDate.of(2027, 2, 28))
                .build());
    }

    private void seedAnnouncements(List<TeamRoster> rosters, UserPrincipal admin) {
        announce(null, "Welcome to the 2025 Season", "We're excited to kick off the new season this January! Please ensure registration fees are settled before the first training.", admin);
        announce(null, "Chinese New Year Break", "The academy will be closed from 2025-01-29 to 2025-02-02 for Chinese New Year. Training resumes as usual after the break.", admin);
        announce(rosters.get(0).team().getId(), "New training kit required", "All Tigers U10 Boys players should collect their new training kit from the office this week.", admin);
        announce(rosters.get(2).team().getId(), "State Cup squad announced", "The squad list for the upcoming Lions U14 Boys Invitational has been posted on the notice board.", admin);
        announce(null, "Hari Raya Aidilfitri Closure", "The academy will be closed for Hari Raya Aidilfitri. Exact dates will be confirmed closer to the date.", admin);
        announce(rosters.get(4).team().getId(), "Parents meeting", "A short parents meeting for Eagles U12 Girls will be held after training this Friday to discuss the upcoming season.", admin);
        announce(null, "Monthly fee reminder", "A friendly reminder that monthly training fees are due by the 5th of each month. Please settle any outstanding balances promptly.", admin);
        announce(null, "New sponsorship announced", "We're proud to welcome City Cafe & Restaurant as our main sponsor for the 2026 season!", admin);
        announce(rosters.get(1).team().getId(), "Field A maintenance", "Training for Tigers U12 Boys is temporarily moved to Field B while Field A undergoes resurfacing.", admin);
        announce(rosters.get(5).team().getId(), "Upcoming tournament", "Eagles U14 Girls will be competing in an upcoming invitational tournament — details to follow.", admin);
        announce(null, "Malaysia Day Closure", "The academy will be closed on 2026-09-16 for Malaysia Day. No trainings or activities that day.", admin);
        announce(rosters.get(3).team().getId(), "Jersey collection", "New match jerseys for Lions U16 Boys are ready for collection at the office.", admin);
        announce(null, "Equipment donation drive", "We're collecting gently-used boots and shin guards for players in need. Drop-offs welcome at the office.", admin);
        announce(null, "End-of-year academy photos", "Academy-wide team photos will be taken during training sessions in the coming weeks. Please arrive in full kit.", admin);
        announce(rosters.get(0).team().getId(), "Coach availability", "Coach for Tigers U10 Boys will be on leave for one week — an assistant coach will cover training sessions.", admin);
    }

    private void announce(Long teamId, String title, String content, UserPrincipal admin) {
        announcementService.create(
                AnnouncementRequest.builder().teamId(teamId).title(title).content(content).build(), admin);
    }

    private UserPrincipal createPerson(String email, String fullName, UserRole role) {
        UserResponse response = userService.create(UserRequest.builder()
                .email(email)
                .password(SAMPLE_PASSWORD)
                .fullName(fullName)
                .role(role)
                .active(true)
                .build());
        User user = userRepository.findById(response.getId()).orElseThrow();
        user.setMustChangePassword(false);
        userRepository.save(user);
        return new UserPrincipal(user);
    }
}
