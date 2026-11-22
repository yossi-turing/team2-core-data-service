package com.therapy.coredataservice.config;

import com.therapy.coredataservice.entity.AnimationTrigger;
import com.therapy.coredataservice.entity.CharacterEntity;
import com.therapy.coredataservice.repository.AnimationTriggerRepository;
import com.therapy.coredataservice.repository.CharacterRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:character_seeder_test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
class CharacterDataSeederTest {

    private static final List<String> EXPECTED_ANIMATION_IDS = List.of(
            "WAVE_HELLO",
            "IDLE_LISTEN",
            "THINKING",
            "CLAP_HAPPY",
            "EMPATHIC_NOD",
            "HUG_GESTURE"
    );

    @Autowired
    private CharacterDataSeeder characterDataSeeder;

    @Autowired
    private CharacterRepository characterRepository;

    @Autowired
    private AnimationTriggerRepository animationTriggerRepository;

    @Test
    void shouldSeedDefaultAnimationTriggersOnStartup() {
        List<AnimationTrigger> triggers = animationTriggerRepository.findAll();

        assertThat(triggers).hasSize(EXPECTED_ANIMATION_IDS.size());
        assertThat(triggers)
                .extracting(AnimationTrigger::getAnimationTriggerId)
                .containsExactlyInAnyOrderElementsOf(EXPECTED_ANIMATION_IDS);
        assertThat(triggers).allSatisfy(trigger -> {
            assertThat(trigger.getDisplayName()).isNotBlank();
            assertThat(trigger.getAiUsageDescription()).isNotBlank();
        });
    }

    @Test
    void shouldSeedDefaultCharactersWithAnimationsOnStartup() {
        List<CharacterEntity> characters = characterRepository.findAll();

        assertThat(characters)
                .extracting(CharacterEntity::getId)
                .containsExactlyInAnyOrder("char_astro", "char_rex", "char_leo");

        CharacterEntity astro = characterRepository.findById("char_astro").orElseThrow();
        assertThat(astro.getName()).isEqualTo("אסטרו החוקר מהחלל");
        assertThat(astro.getDescription()).isNotBlank();
        assertThat(astro.getPersonalityProfilePrompt()).isNotBlank();
        assertThat(astro.getVoiceConfigId()).isEqualTo("voice_astro_he_01");
        assertThat(astro.getUnityModelAssetId()).isEqualTo("Astro3DModel");
        assertThat(astro.getAvailableAnimations())
                .extracting(AnimationTrigger::getAnimationTriggerId)
                .containsExactlyInAnyOrderElementsOf(EXPECTED_ANIMATION_IDS);
    }

    @Test
    void shouldBeIdempotentWhenRunMultipleTimes() {
        long initialCharacterCount = characterRepository.count();
        long initialAnimationCount = animationTriggerRepository.count();

        characterDataSeeder.run();
        characterDataSeeder.run();

        assertThat(characterRepository.count()).isEqualTo(initialCharacterCount);
        assertThat(animationTriggerRepository.count()).isEqualTo(initialAnimationCount);
    }

    @Test
    void shouldSeedCharactersWhenAnimationsAlreadyExistInDb() {
        characterRepository.deleteAll();
        assertThat(characterRepository.count()).isZero();
        assertThat(animationTriggerRepository.count()).isEqualTo(EXPECTED_ANIMATION_IDS.size());

        characterDataSeeder.run();

        assertThat(characterRepository.count()).isEqualTo(3);
        assertThat(animationTriggerRepository.count()).isEqualTo(EXPECTED_ANIMATION_IDS.size());
        CharacterEntity astro = characterRepository.findById("char_astro").orElseThrow();
        assertThat(astro.getAvailableAnimations()).hasSize(EXPECTED_ANIMATION_IDS.size());
    }
}
