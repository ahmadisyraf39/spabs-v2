package com.ahmadisyraf39.spabs_v2.finance.service;

import com.ahmadisyraf39.spabs_v2.common.exception.InvalidRequestException;
import com.ahmadisyraf39.spabs_v2.common.exception.ResourceNotFoundException;
import com.ahmadisyraf39.spabs_v2.finance.dto.request.FeeRecordRequest;
import com.ahmadisyraf39.spabs_v2.finance.dto.response.FeeRecordResponse;
import com.ahmadisyraf39.spabs_v2.finance.dto.response.FeeSummaryResponse;
import com.ahmadisyraf39.spabs_v2.finance.entity.FeeItem;
import com.ahmadisyraf39.spabs_v2.finance.entity.FeeRecord;
import com.ahmadisyraf39.spabs_v2.finance.entity.enums.FeeType;
import com.ahmadisyraf39.spabs_v2.finance.entity.enums.FinanceCategory;
import com.ahmadisyraf39.spabs_v2.finance.entity.enums.FinanceReferenceType;
import com.ahmadisyraf39.spabs_v2.finance.entity.enums.FinanceType;
import com.ahmadisyraf39.spabs_v2.finance.entity.enums.PaymentMethod;
import com.ahmadisyraf39.spabs_v2.finance.entity.enums.PaymentStatus;
import com.ahmadisyraf39.spabs_v2.finance.mapper.FeeRecordMapper;
import com.ahmadisyraf39.spabs_v2.finance.repository.FeeItemRepository;
import com.ahmadisyraf39.spabs_v2.finance.repository.FeeRecordRepository;
import com.ahmadisyraf39.spabs_v2.membership.repository.PlayerParentRepository;
import com.ahmadisyraf39.spabs_v2.player.entity.Player;
import com.ahmadisyraf39.spabs_v2.player.repository.PlayerRepository;
import com.ahmadisyraf39.spabs_v2.security.UserPrincipal;
import com.ahmadisyraf39.spabs_v2.team.entity.Team;
import com.ahmadisyraf39.spabs_v2.team.repository.TeamRepository;
import com.ahmadisyraf39.spabs_v2.user.entity.Parent;
import com.ahmadisyraf39.spabs_v2.user.entity.enums.UserRole;
import com.ahmadisyraf39.spabs_v2.user.repository.ParentRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FeeRecordService {

    private final FeeRecordRepository feeRecordRepository;
    private final FeeItemRepository feeItemRepository;
    private final PlayerRepository playerRepository;
    private final TeamRepository teamRepository;
    private final ParentRepository parentRepository;
    private final PlayerParentRepository playerParentRepository;
    private final FeeRecordMapper feeRecordMapper;
    private final FinanceTransactionService financeTransactionService;

    public FeeRecordResponse assignFee(FeeRecordRequest request, UserPrincipal caller) {
        FeeItem feeItem = findFeeItemById(request.getFeeItemId());
        FeeRecord feeRecord = feeRecordMapper.toEntity(request);
        feeRecord.setPlayer(findPlayerById(request.getPlayerId()));
        feeRecord.setFeeItem(feeItem);
        feeRecord.setTeam(findTeamById(request.getTeamId()));
        feeRecord.setAmount(feeItem.getAmount());
        feeRecord.setStatus(PaymentStatus.UNPAID);
        feeRecord.setPaymentMethod(PaymentMethod.MANUAL);
        return toResponse(feeRecordRepository.save(feeRecord), caller);
    }

    public FeeRecordResponse getById(Long id, UserPrincipal caller) {
        FeeRecord feeRecord = findEntityById(id);
        assertCanView(caller, feeRecord.getPlayer().getId());
        return toResponse(feeRecord, caller);
    }

    public List<FeeRecordResponse> getByPlayer(Long playerId, UserPrincipal caller) {
        assertCanView(caller, playerId);
        return feeRecordRepository.findByPlayerId(playerId).stream()
                .map(feeRecord -> toResponse(feeRecord, caller))
                .toList();
    }

    public List<FeeRecordResponse> getByTeam(Long teamId, UserPrincipal caller) {
        UserRole role = caller.getUser().getRole();
        if (role != UserRole.ADMIN && role != UserRole.SUPER_ADMIN) {
            throw new AccessDeniedException("Not allowed to view a team's fee records");
        }
        return feeRecordRepository.findByTeamId(teamId).stream()
                .map(feeRecord -> toResponse(feeRecord, caller))
                .toList();
    }

    public FeeRecordResponse update(Long id, FeeRecordRequest request, UserPrincipal caller) {
        FeeRecord feeRecord = findEntityById(id);
        FeeItem feeItem = findFeeItemById(request.getFeeItemId());
        feeRecordMapper.updateEntity(feeRecord, request);
        feeRecord.setPlayer(findPlayerById(request.getPlayerId()));
        feeRecord.setFeeItem(feeItem);
        feeRecord.setTeam(findTeamById(request.getTeamId()));
        return toResponse(feeRecordRepository.save(feeRecord), caller);
    }

    public void delete(Long id) {
        feeRecordRepository.delete(findEntityById(id));
    }

    public FeeRecordResponse pay(Long id, UserPrincipal caller) {
        FeeRecord feeRecord = findEntityById(id);
        if (feeRecord.getStatus() == PaymentStatus.PAID) {
            throw new InvalidRequestException("FeeRecord " + id + " is already paid");
        }
        LocalDate today = LocalDate.now();
        feeRecord.setStatus(PaymentStatus.PAID);
        feeRecord.setPaidAt(today);
        feeRecord.setPaymentMethod(PaymentMethod.MANUAL);
        FeeRecord saved = feeRecordRepository.save(feeRecord);

        financeTransactionService.recordLinked(
                FinanceType.INCOME,
                categoryFromFeeType(saved.getFeeItem().getFeeType()),
                saved.getAmount(),
                saved.getFeeItem().getName() + " - " + saved.getPlayer().getFullName(),
                today,
                saved.getId(),
                FinanceReferenceType.FEE_RECORD);

        return toResponse(saved, caller);
    }

    public FeeSummaryResponse getSummaryByTeam(Long teamId, UserPrincipal caller) {
        UserRole role = caller.getUser().getRole();
        if (role != UserRole.ADMIN && role != UserRole.SUPER_ADMIN) {
            throw new AccessDeniedException("Not allowed to view a team's fee summary");
        }
        Team team = findTeamById(teamId);
        List<FeeRecord> records = feeRecordRepository.findByTeamId(teamId);

        BigDecimal totalAmount = BigDecimal.ZERO;
        BigDecimal totalPaid = BigDecimal.ZERO;
        for (FeeRecord record : records) {
            totalAmount = totalAmount.add(record.getAmount());
            if (record.getStatus() == PaymentStatus.PAID) {
                totalPaid = totalPaid.add(record.getAmount());
            }
        }

        return FeeSummaryResponse.builder()
                .teamId(team.getId())
                .teamName(team.getName())
                .recordCount(records.size())
                .totalAmount(totalAmount)
                .totalPaid(totalPaid)
                .totalOutstanding(totalAmount.subtract(totalPaid))
                .build();
    }

    private FinanceCategory categoryFromFeeType(FeeType feeType) {
        return switch (feeType) {
            case REGISTRATION -> FinanceCategory.REGISTRATION_FEE;
            case MONTHLY -> FinanceCategory.MONTHLY_FEE;
            case APPAREL, OTHER -> FinanceCategory.OTHER;
        };
    }

    private FeeRecordResponse toResponse(FeeRecord feeRecord, UserPrincipal caller) {
        FeeRecordResponse response = feeRecordMapper.toResponse(feeRecord);
        response.setOverdue(
                feeRecord.getStatus() == PaymentStatus.UNPAID
                        && feeRecord.getDueDate().isBefore(LocalDate.now()));
        return response;
    }

    private void assertCanView(UserPrincipal caller, Long playerId) {
        UserRole role = caller.getUser().getRole();
        if (role == UserRole.ADMIN || role == UserRole.SUPER_ADMIN) {
            return;
        }
        if (role == UserRole.PARENT) {
            Parent parent = parentRepository
                    .findByUserId(caller.getId())
                    .orElseThrow(() -> new AccessDeniedException("No parent profile for this account"));
            boolean isOwnChild = playerParentRepository.findByParentId(parent.getId()).stream()
                    .anyMatch(pp -> pp.getPlayer().getId().equals(playerId));
            if (isOwnChild) {
                return;
            }
        }
        throw new AccessDeniedException("Not allowed to view this player's fee records");
    }

    private FeeRecord findEntityById(Long id) {
        return feeRecordRepository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FeeRecord not found: " + id));
    }

    private FeeItem findFeeItemById(Long feeItemId) {
        return feeItemRepository
                .findById(feeItemId)
                .orElseThrow(() -> new ResourceNotFoundException("FeeItem not found: " + feeItemId));
    }

    private Player findPlayerById(Long playerId) {
        return playerRepository
                .findById(playerId)
                .orElseThrow(() -> new ResourceNotFoundException("Player not found: " + playerId));
    }

    private Team findTeamById(Long teamId) {
        return teamRepository
                .findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found: " + teamId));
    }
}
