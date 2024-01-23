package mb.aml.test.runner;

import java.util.HashSet;
import java.util.Set;

import mb.log.dagger.DaggerLoggerComponent;
import mb.log.dagger.LoggerComponent;
import mb.log.dagger.LoggerModule;

import mb.resource.dagger.DaggerResourceServiceComponent;
import mb.resource.dagger.DaggerRootResourceServiceComponent;
import mb.resource.dagger.RootResourceServiceModule;
import mb.resource.dagger.ResourceServiceComponent;

import mb.pie.api.TaskDef;
import mb.pie.dagger.DaggerRootPieComponent;
import mb.pie.dagger.RootPieComponent;
import mb.pie.dagger.RootPieModule;
import mb.pie.runtime.PieBuilderImpl;

import mb.spoofax.cli.DaggerSpoofaxCliComponent;
import mb.spoofax.cli.SpoofaxCliComponent;
import mb.spoofax.cli.SpoofaxCli;

import mb.spt.DaggerSptComponent;
import mb.spt.DaggerSptResourcesComponent;
import mb.spt.SptComponent;
import mb.spt.SptResourcesComponent;
import mb.spt.lut.StaticLanguageUnderTestProvider;
import mb.spt.model.LanguageUnderTestImpl;

import mb.accmodlang.DaggerAMLComponent;
import mb.accmodlang.DaggerAMLResourcesComponent;
import mb.accmodlang.AMLComponent;
import mb.accmodlang.AMLResourcesComponent;


public class AMLSptTestRunner {
    public static void main(String[] args) {
        // Resource Components
        final SptResourcesComponent sptResourcesComponent =
            DaggerSptResourcesComponent.create();
        final AMLResourcesComponent amlResourcesComponent =
            DaggerAMLResourcesComponent.create();
        final RootResourceServiceModule resourceServiceModule = new RootResourceServiceModule()
            .addRegistriesFrom(sptResourcesComponent)
            .addRegistriesFrom(amlResourcesComponent);

        // Platform Components
        final LoggerComponent loggerComponent = DaggerLoggerComponent.builder()
            .loggerModule(LoggerModule.stdErrErrorsAndWarnings())
            .build();
        final ResourceServiceComponent resourceServiceComponent = DaggerRootResourceServiceComponent.builder()
            .rootResourceServiceModule(resourceServiceModule)
            .loggerComponent(loggerComponent)
            .build();

        final SpoofaxCliComponent platformComponent = DaggerSpoofaxCliComponent.builder()
            .loggerComponent(loggerComponent)
            .resourceServiceComponent(resourceServiceComponent)
            .build();

        // SPT Component
        final SptComponent sptComponent = DaggerSptComponent.builder()
            .loggerComponent(loggerComponent)
            .sptResourcesComponent(sptResourcesComponent)
            .resourceServiceComponent(resourceServiceComponent)
            .platformComponent(platformComponent)
            .build();

        // AML Component
        final AMLComponent amlComponent = DaggerAMLComponent.builder()
            .loggerComponent(loggerComponent)
            .aMLResourcesComponent(amlResourcesComponent)
            .resourceServiceComponent(resourceServiceComponent)
            .platformComponent(platformComponent)
            .build();

        // PIE component depending on SPT and AML language components
        final RootPieComponent pieComponent = DaggerRootPieComponent.builder()
            .rootPieModule(new RootPieModule(PieBuilderImpl::new, () -> {
                final HashSet<TaskDef<?, ?>> taskDefs = new HashSet<>();
                taskDefs.addAll(sptComponent.getTaskDefs());
                taskDefs.addAll(amlComponent.getTaskDefs());
                return taskDefs;
            }))
            .loggerComponent(loggerComponent)
            .resourceServiceComponent(resourceServiceComponent)
            .build();

        // Wire up SPT to use AML as language under test
        sptComponent.getLanguageUnderTestProviderWrapper().set(
            new StaticLanguageUnderTestProvider(
                new LanguageUnderTestImpl(resourceServiceComponent, amlComponent, pieComponent)
            )
        );

        // Run actual command
        final SpoofaxCli cmd = platformComponent.getSpoofaxCmd();
        final int status = cmd.run(args, sptComponent, pieComponent);

        System.exit(status);
  }
}
