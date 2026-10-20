package com.bsicenter.crm;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import com.bsicenter.crm.service.*;
import com.bsicenter.crm.repository.Db;
import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
class CaptchaRulesTest {
 @Test void malformedChallengeIsRejectedWithoutDatabaseAccess(){Db db=mock(Db.class);var service=new CaptchaService(db);assertThrows(ResponseStatusException.class,()->service.verify("invalid","1"));verifyNoInteractions(db);}
 @Test void challengeIsConsumedAndPersianAnswerIsAccepted(){Db db=mock(Db.class);var id=UUID.randomUUID();Map<String,Object> row=Map.of("answer_hash",AuthService.hash(id+":12"),"expires_at",java.sql.Timestamp.from(Instant.now().plusSeconds(90)));when(db.list(anyString(),eq(id))).thenReturn(List.of(row),List.of());var service=new CaptchaService(db);assertDoesNotThrow(()->service.verify(id.toString(),"۱۲"));assertThrows(ResponseStatusException.class,()->service.verify(id.toString(),"12"));verify(db,times(2)).list(startsWith("delete from captcha_challenges"),eq(id));}
 @Test void expiredAndWrongAnswersAreRejected(){for(boolean expired:List.of(false,true)){Db db=mock(Db.class);var id=UUID.randomUUID();Map<String,Object> row=Map.of("answer_hash",AuthService.hash(id+":2"),"expires_at",java.sql.Timestamp.from(Instant.now().plusSeconds(expired?-1:90)));when(db.list(anyString(),eq(id))).thenReturn(List.of(row));assertThrows(ResponseStatusException.class,()->new CaptchaService(db).verify(id.toString(),expired?"2":"3"));}}
}
