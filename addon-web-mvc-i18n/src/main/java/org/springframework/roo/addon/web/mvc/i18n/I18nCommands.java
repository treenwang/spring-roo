package org.springframework.roo.addon.web.mvc.i18n;

import static org.springframework.roo.shell.OptionContexts.APPLICATION_FEATURE_INCLUDE_CURRENT_MODULE;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Service;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.ComponentContext;
import org.springframework.roo.addon.web.mvc.i18n.components.I18n;
import org.springframework.roo.classpath.ModuleFeatureName;
import org.springframework.roo.classpath.TypeLocationService;
import org.springframework.roo.project.ProjectOperations;
import org.springframework.roo.project.maven.Pom;
import org.springframework.roo.shell.CliAvailabilityIndicator;
import org.springframework.roo.shell.CliCommand;
import org.springframework.roo.shell.CliOption;
import org.springframework.roo.shell.CliOptionMandatoryIndicator;
import org.springframework.roo.shell.CliOptionVisibilityIndicator;
import org.springframework.roo.shell.CommandMarker;
import org.springframework.roo.shell.ShellContext;
import org.springframework.roo.support.logging.HandlerUtils;

/**
 * This class provides necessary commands to be able to include new languages on
 * generated projects.
 *
 * @author Sergio Clares
 * @author Juan Carlos García
 * @since 2.0
 */
@Component
@Service
public class I18nCommands implements CommandMarker {

  private static Logger LOGGER = HandlerUtils.getLogger(I18nCommands.class);

  // ------------ OSGi component attributes ----------------
  private BundleContext context;

  private I18nOperations i18nOperations;
  private ProjectOperations projectOperations;
  private TypeLocationService typeLocationService;

  protected void activate(final ComponentContext context) {
    this.context = context.getBundleContext();
  }

  @CliAvailabilityIndicator({"web mvc language"})
  public boolean isInstallLanguageAvailable() {
    return getI18nOperations().isInstallLanguageCommandAvailable();
  }

  /**
   * This indicator checks if --module parameter should be mandatory or not.
   *
   * If focused module doesn't match with the properties of ModuleFeature
   * APPLICATION, --module parameter should be mandatory.
   *
   * @param shellContext
   * @return
   */
  @CliOptionMandatoryIndicator(command = "web mvc language", params = {"module"})
  public boolean isModuleRequired(ShellContext shellContext) {
    Pom module = getProjectOperations().getFocusedModule();
    if (!isModuleVisible(shellContext)
        || getTypeLocationService().hasModuleFeature(module, ModuleFeatureName.APPLICATION)) {
      return false;
    }
    return true;
  }

  /**
   * This indicator checks if --module parameter should be visible or not.
   *
   * If exists more than one module that match with the properties of
   * ModuleFeature APPLICATION, --module parameter should be visible and
   * mandatory.
   *
   * @param shellContext
   * @return
   */
  @CliOptionVisibilityIndicator(command = "web mvc language", params = {"module"},
      help = "Module parameter is not available if there is only one application module")
  public boolean isModuleVisible(ShellContext shellContext) {
    if (getTypeLocationService().getModuleNames(ModuleFeatureName.APPLICATION).size() > 1) {
      return true;
    }
    return false;
  }

  @CliCommand(
      value = "web mvc language",
      help = "Installs new language in generated project views. Also, could be used to specify the "
          + "default language of the project.")
  public void language(
      @CliOption(
          key = "code",
          mandatory = true,
          help = "The language code for the desired bundle. "
              + "Possible values are: supported languages. Currently `en` (English, default) and `es` (Spanish).") final I18n i18n,
      @CliOption(key = "useAsDefault", mandatory = false,
          help = "Indicates if selected language should be used as default on this application. "
              + "Default: `false`.", specifiedDefaultValue = "true",
          unspecifiedDefaultValue = "false") boolean useAsDefault,
      @CliOption(
          key = "module",
          mandatory = true,
          help = "The application module where to install the language support. "
              + "This option is mandatory if the focus is not set in an application module, that is, a "
              + "module containing an `@SpringBootApplication` class. "
              + "This option is available only if there are more than one application module and none of"
              + " them is focused. "
              + "Default if option not present: the unique 'application' module, or focused 'application'"
              + " module.", unspecifiedDefaultValue = ".",
          optionContext = APPLICATION_FEATURE_INCLUDE_CURRENT_MODULE) Pom module) {

    if (i18n == null) {
      LOGGER.log(Level.INFO, "ERROR: You should provide a valid language code.");
      return;
    }

    // Install language
    getI18nOperations().installLanguage(i18n, useAsDefault, module);
  }


  // Get OSGi services

  public I18nOperations getI18nOperations() {
    if (i18nOperations == null) {
      // Get all Services implement ProjectOperations interface
      try {
        ServiceReference<?>[] references =
            this.context.getAllServiceReferences(I18nOperations.class.getName(), null);

        for (ServiceReference<?> ref : references) {
          i18nOperations = (I18nOperations) this.context.getService(ref);
          return i18nOperations;
        }

        return null;

      } catch (InvalidSyntaxException e) {
        LOGGER.warning("Cannot load ProjectOperations on I18nOperationsImpl.");
        return null;
      }
    } else {
      return i18nOperations;
    }
  }

  public ProjectOperations getProjectOperations() {
    if (projectOperations == null) {
      // Get all Services implement ProjectOperations interface
      try {
        ServiceReference<?>[] references =
            this.context.getAllServiceReferences(ProjectOperations.class.getName(), null);

        for (ServiceReference<?> ref : references) {
          projectOperations = (ProjectOperations) this.context.getService(ref);
          return projectOperations;
        }

        return null;

      } catch (InvalidSyntaxException e) {
        LOGGER.warning("Cannot load ProjectOperations on I18nOperationsImpl.");
        return null;
      }
    } else {
      return projectOperations;
    }
  }

  public TypeLocationService getTypeLocationService() {
    if (typeLocationService == null) {
      // Get all Services implement TypeLocationService interface
      try {
        ServiceReference<?>[] references =
            this.context.getAllServiceReferences(TypeLocationService.class.getName(), null);

        for (ServiceReference<?> ref : references) {
          typeLocationService = (TypeLocationService) this.context.getService(ref);
          return typeLocationService;
        }

        return null;

      } catch (InvalidSyntaxException e) {
        LOGGER.warning("Cannot load TypeLocationService on I18nOperationsImpl.");
        return null;
      }
    } else {
      return typeLocationService;
    }
  }

}
