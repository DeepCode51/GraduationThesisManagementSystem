package cn.jsj.gratuatepager.interceptor.censor;

import cn.jsj.gratuatepager.pojo.CpasswordBoot;
import io.jsonwebtoken.*;
import org.springframework.stereotype.Component;


import java.util.Date;
import java.util.UUID;

@Component
public class TokenCensor {

    public static String[] getToken(Integer userId, String userAccount) {
        String[] tokens = new String[2];
        String randomStr = UUID.randomUUID().toString();
        JwtBuilder jwtBuilder = Jwts.builder().setId(userId.toString()).setSubject(userAccount)
                .setIssuedAt(new Date())
                .signWith(SignatureAlgorithm.HS256, randomStr);
        String token= jwtBuilder.compact();
        tokens[0] = token;
        tokens[1] = randomStr;
        return tokens;
    }

    public static CpasswordBoot parseToken(String tokenStr, String jwtKey) {
        Claims claims = Jwts.parser().setSigningKey(jwtKey).parseClaimsJws(tokenStr).getBody();
        CpasswordBoot dto = new CpasswordBoot();
        dto.setUserId(Integer.parseInt(claims.getId()));
        dto.setUserAccount(claims.getSubject());
        return dto;
    }




}
