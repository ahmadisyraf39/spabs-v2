package com.ahmadisyraf39.spabs_v2.finance.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ahmadisyraf39.spabs_v2.common.exception.InvalidRequestException;
import com.ahmadisyraf39.spabs_v2.finance.dto.request.FeeRecordRequest;
import com.ahmadisyraf39.spabs_v2.finance.dto.response.FeeRecordResponse;
import com.ahmadisyraf39.spabs_v2.finance.dto.response.FeeSummaryResponse;
import com.ahmadisyraf39.spabs_v2.finance.entity.FeeItem;
import com.ahmadisyraf39.spabs_v2.finance.entity.FeeRecord;
import com.ahmadisyraf39.spabs_v2.finance.entity.enums.FeeType;
import com.ahmadisyraf39.spabs_v2.finance.entity.enums.FinanceCategory;
import com.ahmadisyraf39.spabs_v2.finance.entity.enums.FinanceReferenceType;
import com.ahmadisyraf39.spabs_v2.finance.entity.enums.FinanceType;
import com.ahmadisyraf39.spabs_v2.finance.entity.enums.PaymentStatus;
import com.ahmadisyraf39.spabs_v2.finance.mapper.FeeRecordMapper;
import com.ahmadisyraf39.spabs_v2.finance.repository.FeeItemRepository;
import com.ahmadisyraf39.spabs_v2.finance.repository.FeeRecordRepository;
import com.ahmadisyraf39.spabs_v2.membership.entity.PlayerParent;
import com.ahmadisyraf39.spabs_v2.membership.repository.PlayerParentRepository;
import com.ahmadisyraf39.spabs_v2.player.entity.Player;
import com.ahmadisyraf39.spabs_v2.player.repository.PlayerRepository;
import com.ahmadisyraf39.spabs_v2.security.UserPrincipal;
import com.ahmadisyraf39.spabs_v2.team.entity.Team;
import com.ahmadisyraf39.spabs_v2.team.repository.TeamRepository;
import com.ahmadisyraf39.spabs_v2.user.entity.Parent;
import com.ahmadisyraf39.spabs_v2.user.entity.User;
import com.ahmadisyraf39.spabs_v2.user.entity.enums.UserRole;
import com.ahmadisyraf39.spabs_v2.user.repository.ParentRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

@ExtendWith(MockitoExtension.class)
class FeeRecordServiceTest {

    @Mock
    private FeeRecordRepository feeRecordRepository;

    @Mock
    private FeeItemRepository feeItemRepository;

    @Mock
    private PlayerRepository playerRepository;

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private ParentRepository parentRepository;

    @Mock
    private PlayerParentRepository playerParentRepository;

    @Mock
    private FeeRecordMapper feeRecordMapper;

    @Mock
    private FinanceTransactionService financeTransactionService;

    @InjectMocks
    private FeeRecordService feeRecordService;

    private UserPrincipal principal(UserRole role, Long userId) {
        User user = User.builder()
                .id(userId)
                .email(role + "@spabs.example")
                .fullName("Test")
                .role(role)
                .build();
        return new UserPrincipal(user);
    }

    @Test
    void assignFee_createsUnpaidRecordWithAmountCopiedFromFeeItem() {
        UserPrincipal admin = principal(UserRole.ADMIN, 1L);
        FeeItem feeItem = FeeItem.builder()
                .id(1L)
                .name("Monthly Fee")
                .feeType(FeeType.MONTHLY)
                .amount(new BigDecimal("120.00"))
                .build();
        when(feeItemRepository.findById(1L)).thenReturn(Optional.of(feeItem));
        when(playerRepository.findById(5L)).thenReturn(Optional.of(Player.builder().id(5L).build()));
        when(teamRepository.findById(10L)).thenReturn(Optional.of(Team.builder().id(10L).build()));
        FeeRecord entity = new FeeRecord();
        entity.setDueDate(LocalDate.now().plusDays(30));
        when(feeRecordMapper.toEntity(any())).thenReturn(entity);
        when(feeRecordRepository.save(entity)).thenReturn(entity);
        when(feeRecordMapper.toResponse(entity)).thenReturn(FeeRecordResponse.builder().build());

        FeeRecordRequest request = FeeRecordRequest.builder()
                .playerId(5L)
                .feeItemId(1L)
                .teamId(10L)
                .dueDate(LocalDate.now().plusDays(30))
                .build();

        feeRecordService.assignFee(request, admin);

        assertThat(entity.getStatus()).isEqualTo(PaymentStatus.UNPAID);
        assertThat(entity.getAmount()).isEqualByComparingTo("120.00");
    }

    @Test
    void pay_alreadyPaid_throwsInvalidRequestException() {
        FeeRecord existing = FeeRecord.builder().id(1L).status(PaymentStatus.PAID).build();
        when(feeRecordRepository.findById(1L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> feeRecordService.pay(1L, principal(UserRole.ADMIN, 1L)))
                .isInstanceOf(InvalidRequestException.class);
    }

    @Test
    void pay_marksPaidAndRecordsLinkedIncomeWithCorrectCategory() {
        FeeItem feeItem = FeeItem.builder()
                .id(1L)
                .name("Monthly Fee")
                .feeType(FeeType.MONTHLY)
                .amount(new BigDecimal("120.00"))
                .build();
        Player player = Player.builder().id(5L).fullName("Ali").build();
        FeeRecord existing = FeeRecord.builder()
                .id(1L)
                .status(PaymentStatus.UNPAID)
                .feeItem(feeItem)
                .player(player)
                .amount(new BigDecimal("120.00"))
                .build();
        when(feeRecordRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(feeRecordRepository.save(existing)).thenReturn(existing);
        when(feeRecordMapper.toResponse(existing)).thenReturn(FeeRecordResponse.builder().build());

        feeRecordService.pay(1L, principal(UserRole.ADMIN, 1L));

        assertThat(existing.getStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(existing.getPaidAt()).isEqualTo(LocalDate.now());
        verify(financeTransactionService)
                .recordLinked(
                        eq(FinanceType.INCOME),
                        eq(FinanceCategory.MONTHLY_FEE),
                        eq(new BigDecimal("120.00")),
                        any(),
                        eq(LocalDate.now()),
                        eq(1L),
                        eq(FinanceReferenceType.FEE_RECORD));
    }

    @Test
    void getByTeam_asNonAdmin_isDenied() {
        assertThatThrownBy(() -> feeRecordService.getByTeam(10L, principal(UserRole.PARENT, 3L)))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void getByTeam_asAdmin_isAllowed() {
        when(feeRecordRepository.findByTeamId(10L)).thenReturn(List.of());

        List<FeeRecordResponse> result = feeRecordService.getByTeam(10L, principal(UserRole.ADMIN, 1L));

        assertThat(result).isEmpty();
    }

    @Test
    void getById_asParentOwningPlayer_isAllowed() {
        FeeRecord record = FeeRecord.builder()
                .id(1L)
                .player(Player.builder().id(5L).build())
                .status(PaymentStatus.UNPAID)
                .dueDate(LocalDate.now().plusDays(1))
                .build();
        when(feeRecordRepository.findById(1L)).thenReturn(Optional.of(record));
        Parent parentProfile = Parent.builder().id(30L).build();
        when(parentRepository.findByUserId(3L)).thenReturn(Optional.of(parentProfile));
        PlayerParent link = PlayerParent.builder()
                .player(Player.builder().id(5L).build())
                .build();
        when(playerParentRepository.findByParentId(30L)).thenReturn(List.of(link));
        when(feeRecordMapper.toResponse(record)).thenReturn(FeeRecordResponse.builder().build());

        FeeRecordResponse response = feeRecordService.getById(1L, principal(UserRole.PARENT, 3L));

        assertThat(response).isNotNull();
    }

    @Test
    void getById_asParentNotOwningPlayer_isDenied() {
        FeeRecord record =
                FeeRecord.builder().id(1L).player(Player.builder().id(5L).build()).build();
        when(feeRecordRepository.findById(1L)).thenReturn(Optional.of(record));
        Parent parentProfile = Parent.builder().id(30L).build();
        when(parentRepository.findByUserId(3L)).thenReturn(Optional.of(parentProfile));
        when(playerParentRepository.findByParentId(30L)).thenReturn(List.of());

        assertThatThrownBy(() -> feeRecordService.getById(1L, principal(UserRole.PARENT, 3L)))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void toResponse_unpaidPastDueDate_isFlaggedOverdue() {
        FeeRecord record = FeeRecord.builder()
                .id(1L)
                .player(Player.builder().id(5L).build())
                .status(PaymentStatus.UNPAID)
                .dueDate(LocalDate.now().minusDays(1))
                .build();
        when(feeRecordRepository.findById(1L)).thenReturn(Optional.of(record));
        when(feeRecordMapper.toResponse(record)).thenReturn(FeeRecordResponse.builder().build());

        FeeRecordResponse response = feeRecordService.getById(1L, principal(UserRole.ADMIN, 1L));

        assertThat(response.isOverdue()).isTrue();
    }

    @Test
    void toResponse_paidPastDueDate_isNotOverdue() {
        FeeRecord record = FeeRecord.builder()
                .id(1L)
                .player(Player.builder().id(5L).build())
                .status(PaymentStatus.PAID)
                .dueDate(LocalDate.now().minusDays(1))
                .build();
        when(feeRecordRepository.findById(1L)).thenReturn(Optional.of(record));
        when(feeRecordMapper.toResponse(record)).thenReturn(FeeRecordResponse.builder().build());

        FeeRecordResponse response = feeRecordService.getById(1L, principal(UserRole.ADMIN, 1L));

        assertThat(response.isOverdue()).isFalse();
    }

    @Test
    void getSummaryByTeam_computesTotalsCorrectly() {
        when(teamRepository.findById(10L)).thenReturn(Optional.of(Team.builder().id(10L).name("Tigers").build()));
        FeeRecord paid = FeeRecord.builder()
                .status(PaymentStatus.PAID)
                .amount(new BigDecimal("100.00"))
                .build();
        FeeRecord unpaid = FeeRecord.builder()
                .status(PaymentStatus.UNPAID)
                .amount(new BigDecimal("50.00"))
                .build();
        when(feeRecordRepository.findByTeamId(10L)).thenReturn(List.of(paid, unpaid));

        FeeSummaryResponse summary = feeRecordService.getSummaryByTeam(10L, principal(UserRole.ADMIN, 1L));

        assertThat(summary.getTotalAmount()).isEqualByComparingTo("150.00");
        assertThat(summary.getTotalPaid()).isEqualByComparingTo("100.00");
        assertThat(summary.getTotalOutstanding()).isEqualByComparingTo("50.00");
    }

    @Test
    void getSummaryByTeam_asNonAdmin_isDenied() {
        assertThatThrownBy(() -> feeRecordService.getSummaryByTeam(10L, principal(UserRole.COACH, 2L)))
                .isInstanceOf(AccessDeniedException.class);
    }
}
