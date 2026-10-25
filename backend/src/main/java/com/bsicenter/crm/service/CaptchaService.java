package com.bsicenter.crm.service;

import com.bsicenter.crm.repository.Db;
import com.bsicenter.crm.exception.ApiError;
import java.security.SecureRandom;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

/** Database-backed, one-use challenge: works across server replicas and restarts. */
@Service
public class CaptchaService {
  private final Db db; private final SecureRandom random=new SecureRandom();
  public CaptchaService(Db db){this.db=db;}
  private String svg(String shape){
    return "data:image/svg+xml;base64,"+Base64.getEncoder().encodeToString(("<svg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 100 80'><rect width='100' height='80' rx='16' fill='#ecf7f4'/><g fill='#167d71' stroke='#125f57' stroke-width='2'>"+shape+"</g></svg>").getBytes(java.nio.charset.StandardCharsets.UTF_8));
  }
  public Object create(String ip,String mode){
    db.run("delete from captcha_challenges where expires_at<now()");
    UUID id=UUID.randomUUID();String answer;Map<String,Object> out=new LinkedHashMap<>();out.put("id",id.toString());out.put("expiresIn",180);
    if("text".equals(mode)){
      int x=2+random.nextInt(20),y=1+random.nextInt(10);answer=""+(x+y);
      out.put("mode","text");out.put("question",x+" + "+y+" = ؟");
    }else{
      var shapes=new ArrayList<>(List.of("<circle cx='50' cy='40' r='24'/>","<rect x='26' y='16' width='48' height='48' rx='4'/>","<path d='M50 12 L78 66 H22 Z'/>","<path d='M50 10 L80 40 L50 70 L20 40 Z'/>","<path d='M40 12 H60 V30 H78 V50 H60 V68 H40 V50 H22 V30 H40 Z'/>","<path d='M50 10 L60 30 L82 34 L66 49 L70 72 L50 61 L30 72 L34 49 L18 34 L40 30 Z'/>"));
      Collections.shuffle(shapes,random);int selected=random.nextInt(shapes.size());answer=""+selected;
      out.put("mode","puzzle");out.put("question","قطعه‌ای را انتخاب کنید که با شکل نمونه یکسان است");
      out.put("image",svg(shapes.get(selected)));out.put("choices",shapes.stream().map(this::svg).toList());
    }
    db.run("insert into captcha_challenges(id,answer_hash,expires_at) values(?,?,now()+interval '3 minutes')",id,AuthService.hash(id+":"+answer));
    return out;
  }
  public void verify(String id,String answer){
    UUID uuid;try{uuid=UUID.fromString(id);}catch(Exception e){throw ApiError.bad("کپچا نادرست یا منقضی شده است");}
    var rows=db.list("delete from captcha_challenges where id=? returning answer_hash,expires_at",uuid);
    String normalized=com.bsicenter.crm.validation.CustomerIdentity.digits(Objects.toString(answer,"").trim());
    if(rows.isEmpty() || ((java.sql.Timestamp)rows.get(0).get("expires_at")).toInstant().isBefore(java.time.Instant.now()) || !java.security.MessageDigest.isEqual(rows.get(0).get("answer_hash").toString().getBytes(java.nio.charset.StandardCharsets.UTF_8),AuthService.hash(id+":"+normalized).getBytes(java.nio.charset.StandardCharsets.UTF_8)))
      throw ApiError.bad("کپچا نادرست یا منقضی شده است؛ چالش جدید را حل کنید");
  }
}
