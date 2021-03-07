package com.rhwngh.board.service;

import com.rhwngh.board.UserCustom;
import com.rhwngh.board.domain.Role;
import com.rhwngh.board.domain.entity.MemberEntity;
import com.rhwngh.board.domain.repository.MemberRepository;
import com.rhwngh.board.dto.MemberDto;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@Service
@AllArgsConstructor
public class MemberService implements UserDetailsService {
    private MemberRepository memberRepository;

    @Transactional
    public Long joinUser(MemberDto memberDto) {

        String userEmail = memberDto.getEmail();

        Optional<MemberEntity> userEntityWrapper = memberRepository.findByEmail(userEmail);
        if(!userEntityWrapper.isEmpty()) {
            return 11l;
        }

        // 비밀번호 암호화
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        memberDto.setPassword(passwordEncoder.encode(memberDto.getPassword()));

        return memberRepository.save(memberDto.toEntity()).getId();
    }

    @Override
    public UserDetails loadUserByUsername(String userEmail) throws UsernameNotFoundException { // 로그인을 하는 form에서 name="username"으로 요청
        boolean enabled = true;
        boolean accountNonExpired = true;
        boolean credentialsNonExpired = true;
        boolean accountNonLocked = true;

        Optional<MemberEntity> userEntityWrapper = memberRepository.findByEmail(userEmail);
        MemberEntity userEntity = userEntityWrapper.get();

        List<GrantedAuthority> authorities = new ArrayList<>();

        if (("admin@example.com").equals(userEmail)) {
            authorities.add(new SimpleGrantedAuthority(Role.ADMIN.getValue()));
        } else {
            authorities.add(new SimpleGrantedAuthority(Role.MEMBER.getValue()));
        }

        UserCustom userCustom = new UserCustom(userEntity.getEmail(), userEntity.getPassword(), enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities, userEntity.getName());

        return userCustom;
    }
}
