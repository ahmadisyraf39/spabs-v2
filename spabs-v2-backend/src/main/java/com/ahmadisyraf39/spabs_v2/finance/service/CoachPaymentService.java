package com.ahmadisyraf39.spabs_v2.finance.service;

import com.ahmadisyraf39.spabs_v2.common.exception.InvalidRequestException;
import com.ahmadisyraf39.spabs_v2.common.exception.ResourceNotFoundException;
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
import com.ahmadisyraf39.spabs_v2.user.entity.enums.UserRole;
import com.ahmadisyraf39.spabs_v2.user.repository.CoachRepository;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CoachPaymentService {

    private final CoachPaymentRepository coachPaymentRepository;
    private final CoachRepository coachRepository;
    private final CoachPaymentMapper coachPaymentMapper;
    private final FinanceTransactionService financeTransactionService;

    public CoachPaymentResponse create(CoachPaymentRequest request) {
        CoachPayment coachPayment = coachPaymentMapper.toEntity(request);
        coachPayment.setCoach(findCoachById(request.getCoachId()));
        coachPayment.setStatus(PaymentStatus.UNPAID);
        return coachPaymentMapper.toResponse(coachPaymentRepository.save(coachPayment));
    }

    public CoachPaymentResponse getById(Long id, UserPrincipal caller) {
        CoachPayment coachPayment = findEntityById(id);
        assertCanView(caller, coachPayment.getCoach().getId());
        return coachPaymentMapper.toResponse(coachPayment);
    }

    public List<CoachPaymentResponse> getAll(UserPrincipal caller) {
        UserRole role = caller.getUser().getRole();
        if (role != UserRole.ADMIN && role != UserRole.SUPER_ADMIN) {
            throw new AccessDeniedException("Not allowed to view all coach payments");
        }
        return coachPaymentRepository.findAll().stream()
                .map(coachPaymentMapper::toResponse)
                .toList();
    }

    public List<CoachPaymentResponse> getByCoach(Long coachId, UserPrincipal caller) {
        assertCanView(caller, coachId);
        return coachPaymentRepository.findByCoachId(coachId).stream()
                .map(coachPaymentMapper::toResponse)
                .toList();
    }

    private void assertCanView(UserPrincipal caller, Long coachId) {
        UserRole role = caller.getUser().getRole();
        if (role == UserRole.COACH) {
            Coach ownCoach = coachRepository
                    .findByUserId(caller.getId())
                    .orElseThrow(() -> new AccessDeniedException("No coach profile for this account"));
            if (!ownCoach.getId().equals(coachId)) {
                throw new AccessDeniedException("Not allowed to view another coach's payments");
            }
        } else if (role != UserRole.ADMIN && role != UserRole.SUPER_ADMIN) {
            throw new AccessDeniedException("Not allowed to view coach payments");
        }
    }

    public CoachPaymentResponse update(Long id, CoachPaymentRequest request) {
        CoachPayment coachPayment = findEntityById(id);
        coachPaymentMapper.updateEntity(coachPayment, request);
        coachPayment.setCoach(findCoachById(request.getCoachId()));
        return coachPaymentMapper.toResponse(coachPaymentRepository.save(coachPayment));
    }

    public void delete(Long id) {
        coachPaymentRepository.delete(findEntityById(id));
    }

    public CoachPaymentResponse pay(Long id) {
        CoachPayment coachPayment = findEntityById(id);
        if (coachPayment.getStatus() == PaymentStatus.PAID) {
            throw new InvalidRequestException("CoachPayment " + id + " is already paid");
        }
        coachPayment.setStatus(PaymentStatus.PAID);
        CoachPayment saved = coachPaymentRepository.save(coachPayment);

        financeTransactionService.recordLinked(
                FinanceType.EXPENSE,
                categoryFromPaymentType(saved.getPaymentType()),
                saved.getAmount(),
                "Coach payment - " + saved.getCoach().getUser().getFullName(),
                LocalDate.now(),
                saved.getId(),
                FinanceReferenceType.COACH_PAYMENT);

        return coachPaymentMapper.toResponse(saved);
    }

    private FinanceCategory categoryFromPaymentType(CoachPaymentType paymentType) {
        return switch (paymentType) {
            case SALARY -> FinanceCategory.COACH_SALARY;
            case PER_SESSION -> FinanceCategory.COACH_PER_SESSION;
            case BONUS -> FinanceCategory.COACH_BONUS;
        };
    }

    private CoachPayment findEntityById(Long id) {
        return coachPaymentRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CoachPayment not found: " + id));
    }

    private Coach findCoachById(Long coachId) {
        return coachRepository
                .findById(coachId)
                .orElseThrow(() -> new ResourceNotFoundException("Coach not found: " + coachId));
    }
}
