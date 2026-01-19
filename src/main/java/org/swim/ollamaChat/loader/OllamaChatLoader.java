package org.swim.ollamaChat.loader;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;

public final class OllamaChatLoader implements PluginLoader {
    @Override
    public void classloader(PluginClasspathBuilder classpathBuilder) {
        MavenLibraryResolver resolver = new MavenLibraryResolver();
        resolver.addDependency(new Dependency(
            new DefaultArtifact("com.google.code.gson:gson:2.10.1"),
            null
        ));
        resolver.addRepository(new RemoteRepository.Builder(
            "paper",
            "default",
            "https://repo.papermc.io/repository/maven-public/"
        ).build());
        resolver.addRepository(new RemoteRepository.Builder(
            "central",
            "default",
            MavenLibraryResolver.MAVEN_CENTRAL_DEFAULT_MIRROR
        ).build());

        classpathBuilder.addLibrary(resolver);
    }
}
