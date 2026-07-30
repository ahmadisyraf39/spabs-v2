package com.ahmadisyraf39.spabs_v2.finance.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.ahmadisyraf39.spabs_v2.common.exception.InvalidRequestException;
import com.ahmadisyraf39.spabs_v2.finance.dto.request.CoachPaymentRequest;
import com.ahmadisyraf39.spabs_v2.finance.dto.response.CoachPaymentResponse;
import com.ahmadisyraf39.spabs_v2.finance.entity.CoachPayment;
import com.ahmadisyraf39.spabs_v2.finance.entity.enums.CoachPaymentType;
import com.ahmadisyraf39.spabs_v2.finance.entity.enums.FinanceCategory;
import com.ahmadisyraf39.spabs_v2.finance.entity.enums.FinanceReferenceType;
import com.ahmadisyraf39.spabs_v2.finance.entity.enums.FinanceType;
import com.ahmadisyraf39.spabs_v2.finance.entity.enums.PaymentStatus;
import com.ahmadisyraf39.spabs_v2.finance.mapper.CoachPaymentMapper;
import com.ahmadisyraf39.spabs_v2.finance.repository.CoachPaymentRepository;
import com.ahmadisyraf39.spabs_v2.security.UserPrincipal;
import com.ahmadisyraf39.spabs_v2.user.entity.Coach;
import com.ahmadisyraf39.spabs_v2.user.entity.User;
import com.ahmadisyraf39.spabs_v2.user.entity.enums.UserRole;
import com.ahmadisyraf39.spabs_v2.user.repository.CoachRepository;
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
class CoachPaymentServiceTest {

    @Mock
    private CoachPaymentRepository coachPaymentRepository;

    @Mock
    private CoachRepository coachRepository;

    @Mock
    private CoachPaymentMapper coachPaymentMapper;

    @Mock
    private FinanceTransactionService financeTransactionService;

    @InjectMocks
    private CoachPaymentService coachPaymentService;

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
    void create_setsStatusUnpaid() {
        Coach coach = Coach.builder().id(20L).build();
        when(coachRepository.findById(20L)).thenReturn(Optional.of(coach));
        CoachPayment entity = new CoachPayment();
        when(coachPaymentMapper.toEntity(any())).thenReturn(entity);
        when(coachPaymentRepository.save(entity)).thenReturn(entity);
        when(coachPaymentMapper.toResponse(entity)).thenReturn(CoachPaymentResponse.builder().build());

        CoachPaymentRequest request = CoachPaymentRequest.builder()
                .coachId(20L)
                .amount(new BigDecimal("500.00"))
                .paymentDate(LocalDate.now())
                .paymentType(CoachPaymentType.SALARY)
                .build();

        coachPaymentService.create(request);

        assertThat(entity.getStatus()).isEqualTo(PaymentStatus.UNPAID);
    }

    @Test
    void getAll_asNonAdmin_isDenied() {
        assertThatThrownBy(() -> coachPaymentService.getAll(principal(UserRole.COACH, 2L)))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void getAll_asAdmin_isAllowed() {
        when(coachPaymentRepository.findAll()).thenReturn(List.of());

        List<CoachPaymentResponse> result = coachPaymentService.getAll(principal(UserRole.ADMIN, 1L));

        assertThat(result).isEmpty();
    }

    @Test
    void getByCoach_asAdmin_canViewAnyCoach() {
        when(coachPaymentRepository.findByCoachId(20L)).thenReturn(List.of());

        List<CoachPaymentResponse> result = coachPaymentService.getByCoach(20L, principal(UserRole.ADMIN, 1L));

        assertThat(result).isEmpty();
    }

    @Test
    void getByCoach_asCoachViewingOwnPayments_isAllowed() {
        Coach ownCoach = Coach.builder().id(20L).build();
        when(coachRepository.findByUserId(2L)).thenReturn(Optional.of(ownCoach));
        when(coachPaymentRepository.findByCoachId(20L)).thenReturn(List.of());

        List<CoachPaymentResponse> result = coachPaymentService.getByCoach(20L, principal(UserRole.COACH, 2L));

        assertThat(result).isEmpty();
    }

    // Regression test for a real FAMS gap: any authenticated coach could query any
    // coachId's payment history.
    @Test
    void getByCoach_asCoachViewingAnotherCoachesPayments_isDenied() {
        Coach ownCoach = Coach.builder().id(21L).build();
        when(coachRepository.findByUserId(2L)).thenReturn(Optional.of(ownCoach));

        assertThatThrownBy(() -> coachPaymentService.getByCoach(20L, principal(UserRole.COACH, 2L)))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void getByCoach_asParent_isDenied() {
        assertThatThrownBy(() -> coachPaymentService.getByCoach(20L, principal(UserRole.PARENT, 3L)))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void pay_alreadyPaid_throws() {
        CoachPayment existing = CoachPayment.builder().id(1L).status(PaymentStatus.PAID).build();
        when(coachPaymentRepository.findById(1L)).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> coachPaymentService.pay(1L)).isInstanceOf(InvalidRequestException.class);
    }

    @Test
    void pay_salary_recordsLinkedExpenseWithSalaryCategory() {
        Coach coach = Coach.builder()
                .id(20L)
                .user(User.builder().fullName("Coach Tigers").build())
                .build();
        CoachPayment existing = CoachPayment.builder()
                .id(1L)
                .status(PaymentStatus.UNPAID)
                .coach(coach)
                .amount(new BigDecimal("800.00"))
                .paymentType(CoachPaymentType.SALARY)
                .build();
        when(coachPaymentRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(coachPaymentRepository.save(existing)).thenReturn(existing);
        when(coachPaymentMapper.toResponse(existing)).thenReturn(CoachPaymentResponse.builder().build());

        coachPaymentService.pay(1L);

        assertThat(existing.getStatus()).isEqualTo(PaymentStatus.PAID);
        verify(financeTransactionService)
                .recordLinked(
                        eq(FinanceType.EXPENSE),
                        eq(FinanceCategory.COACH_SALARY),
                        eq(new BigDecimal("800.00")),
                        any(),
                        eq(LocalDate.now()),
                        eq(1L),
                        eq(FinanceReferenceType.COACH_PAYMENT));
    }

    @Test
    void pay_perSession_recordsLinkedExpenseWithPerSessionCategory() {
        Coach coach = Coach.builder()
                .id(20L)
                .user(User.builder().fullName("Assistant Coach").build())
                .build();
        CoachPayment existing = CoachPayment.builder()
                .id(2L)
                .status(PaymentStatus.UNPAID)
                .coach(coach)
                .amount(new BigDecimal("150.00"))
                .paymentType(CoachPaymentType.PER_SESSION)
                .build();
        when(coachPaymentRepository.findById(2L)).thenReturn(Optional.of(existing));
        when(coachPaymentRepository.save(existing)).thenReturn(existing);
        when(coachPaymentMapper.toResponse(existing)).thenReturn(CoachPaymentResponse.builder().build());

        coachPaymentService.pay(2L);

        verify(financeTransactionService)
                .recordLinked(
                        eq(FinanceType.EXPENSE),
                        eq(FinanceCategory.COACH_PER_SESSION),
                        any(),
                        any(),
                        any(),
                        eq(2L),
                        eq(FinanceReferenceType.COACH_PAYMENT));
    }

    @Test
    void pay_bonus_recordsLinkedExpenseWithBonusCategory() {
        Coach coach = Coach.builder()
                .id(20L)
                .user(User.builder().fullName("Coach Bonus").build())
                .build();
        CoachPayment existing = CoachPayment.builder()
                .id(3L)
                .status(PaymentStatus.UNPAID)
                .coach(coach)
                .amount(new BigDecimal("50.00"))
                .paymentType(CoachPaymentType.BONUS)
                .build();
        when(coachPaymentRepository.findById(3L)).thenReturn(Optional.of(existing));
        when(coachPaymentRepository.save(existing)).thenReturn(existing);
        when(coachPaymentMapper.toResponse(existing)).thenReturn(CoachPaymentResponse.builder().build());

        coachPaymentService.pay(3L);

        verify(financeTransactionService)
                .recordLinked(
                        eq(FinanceType.EXPENSE),
                        eq(FinanceCategory.COACH_BONUS),
                        any(),
                        any(),
                        any(),
                        eq(3L),
                        eq(FinanceReferenceType.COACH_PAYMENT));
    }
}
