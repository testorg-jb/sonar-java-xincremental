/*
 * SonarQube Java
 * Copyright (C) 2012-2022 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.plugins.java;

import com.sonar.plugins.security.api.JavaRules;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.sonar.api.rule.RuleKey;
import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition;
import org.sonar.api.utils.ValidationMessages;

import static org.assertj.core.api.Assertions.assertThat;

class JavaSonarWayProfileTest {

  @Test
  void should_create_sonar_way_profile() {
    ValidationMessages validation = ValidationMessages.create();

    JavaSonarWayProfile profileDef = new JavaSonarWayProfile();
    BuiltInQualityProfilesDefinition.Context context = new BuiltInQualityProfilesDefinition.Context();
    profileDef.define(context);
    BuiltInQualityProfilesDefinition.BuiltInQualityProfile profile = context.profile("java", "Sonar way");
    assertThat(profile.language()).isEqualTo(Java.KEY);
    List<BuiltInQualityProfilesDefinition.BuiltInActiveRule> activeRules = profile.rules();
    assertThat(activeRules.stream().filter(r -> r.repoKey().equals("common-java"))).hasSize(1);
    assertThat(activeRules).as("Expected number of rules in profile").hasSizeGreaterThanOrEqualTo(268);
    assertThat(profile.name()).isEqualTo("Sonar way");
    Set<String> keys = new HashSet<>();
    for (BuiltInQualityProfilesDefinition.BuiltInActiveRule activeRule : activeRules) {
      keys.add(activeRule.ruleKey());
    }
    //We no longer store active rules with legacy keys, only RSPEC keys are used.
    assertThat(keys).doesNotContain("S00116")
      .contains("S116");
    assertThat(validation.hasErrors()).isFalse();

    // Check that we use severity from the read rule and not default one.
    assertThat(activeRules.get(0).overriddenSeverity()).isNull();
  }

  @Test
  void should_activate_hotspots_when_supported() {
    JavaSonarWayProfile profileDef = new JavaSonarWayProfile();
    BuiltInQualityProfilesDefinition.Context context = new BuiltInQualityProfilesDefinition.Context();
    profileDef.define(context);
    BuiltInQualityProfilesDefinition.BuiltInQualityProfile profile = context.profile("java", "Sonar way");
    BuiltInQualityProfilesDefinition.BuiltInActiveRule rule = profile.rule(RuleKey.of("java", "S2092"));
    assertThat(rule).isNotNull();
  }

  @Test
  void should_contains_security_rules_if_present() {
    // no security rules available
    JavaRules.ruleKeys = new HashSet<>();
    assertThat(JavaSonarWayProfile.getSecurityRuleKeys(true)).isEmpty();

    // one security rule available
    JavaRules.ruleKeys = new HashSet<>(Arrays.asList("S3649"));
    assertThat(JavaSonarWayProfile.getSecurityRuleKeys(true)).containsOnly(RuleKey.of("java", "S3649"));
    assertThat(JavaSonarWayProfile.getSecurityRuleKeys(false)).containsOnly(RuleKey.of("security-repo-key", "S3649"));
  }

}
