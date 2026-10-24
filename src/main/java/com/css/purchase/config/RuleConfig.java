package com.css.purchase.config;

import com.css.purchase.domain.Rule;
import com.css.purchase.domain.TransitionRule;
import com.css.purchase.domain.UnapprovedCannotOrder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Wires the rule chain. Spring lives here, the domain does not know about it. */
@Configuration
public class RuleConfig {

  @Bean
  public Rule requestRule() {
    return new UnapprovedCannotOrder(new TransitionRule());
  }
}
