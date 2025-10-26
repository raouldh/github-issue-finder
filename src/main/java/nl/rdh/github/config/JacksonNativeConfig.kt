package nl.rdh.github.config

import com.fasterxml.jackson.databind.DeserializationFeature
import org.springframework.aot.hint.MemberCategory
import org.springframework.aot.hint.RuntimeHints
import org.springframework.aot.hint.RuntimeHintsRegistrar
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.ImportRuntimeHints
import nl.rdh.github.client.model.Creator
import nl.rdh.github.client.model.Issue
import nl.rdh.github.client.model.Label
import nl.rdh.github.client.model.License
import nl.rdh.github.client.model.Milestone
import nl.rdh.github.client.model.Owner
import nl.rdh.github.client.model.Permissions
import nl.rdh.github.client.model.PullRequest
import nl.rdh.github.client.model.Reactions
import nl.rdh.github.client.model.Repository
import nl.rdh.github.client.model.User

/**
 * Native-image support for Jackson data binding of our GitHub API model.
 *
 * Spring Boot 3 AOT can infer many hints automatically, but Kotlin data class constructor
 * access may still require explicit reflection registration when deserializing JSON coming
 * from the GitHub API in native images. This registrar ensures constructors/fields/methods
 * are available for reflection at runtime.
 *
 * @author Raoul de Haard
 */
class GithubModelRuntimeHints : RuntimeHintsRegistrar {
    override fun registerHints(hints: RuntimeHints, classLoader: ClassLoader?) {
        val modelTypes = listOf(
            Repository::class.java,
            Issue::class.java,
            Label::class.java,
            License::class.java,
            Milestone::class.java,
            Owner::class.java,
            Permissions::class.java,
            PullRequest::class.java,
            Reactions::class.java,
            User::class.java,
            Creator::class.java,
        )

        val memberCategories = arrayOf(
            MemberCategory.INVOKE_DECLARED_CONSTRUCTORS,
            MemberCategory.INVOKE_PUBLIC_CONSTRUCTORS,
            MemberCategory.DECLARED_FIELDS,
            MemberCategory.PUBLIC_FIELDS,
            MemberCategory.INVOKE_DECLARED_METHODS,
            MemberCategory.INVOKE_PUBLIC_METHODS,
        )

        modelTypes.forEach { type ->
            hints.reflection().registerType(type, *memberCategories)
        }
    }
}

@Configuration
@ImportRuntimeHints(GithubModelRuntimeHints::class)
class JacksonNativeConfig {

    /**
     * Be lenient towards unknown properties as the GitHub API evolves frequently.
     * This keeps our models stable without breaking when new fields are introduced.
     */
    @Bean
    fun objectMapperCustomizer(): Jackson2ObjectMapperBuilderCustomizer =
        Jackson2ObjectMapperBuilderCustomizer { builder ->
            builder.featuresToDisable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        }
}
