package net.binis.codegen.scan;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class PrototypeScanner {

	private final ApplicationContext context;

	public PrototypeScanner(ApplicationContext context) {
		Assert.notNull(context, "Context must not be null");
		this.context = context;
	}

	@SafeVarargs
	public final Set<Class<?>> scan(Class<? extends Annotation>... annotationTypes) throws ClassNotFoundException {
		List<String> packages = getPackages();
		if (packages.isEmpty()) {
			return Collections.emptySet();
		}
		ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
		scanner.setEnvironment(this.context.getEnvironment());
		scanner.setResourceLoader(this.context);
		for (Class<? extends Annotation> annotationType : annotationTypes) {
			scanner.addIncludeFilter(new AnnotationTypeFilter(annotationType));
		}
		Set<Class<?>> entitySet = new HashSet<>();
		for (String basePackage : packages) {
			if (StringUtils.hasText(basePackage)) {
				for (BeanDefinition candidate : scanner.findCandidateComponents(basePackage)) {
					entitySet.add(ClassUtils.forName(candidate.getBeanClassName(), this.context.getClassLoader()));
				}
			}
		}
		return entitySet;
	}

	private List<String> getPackages() {
		return null;
//		List<String> packages = PrototypeScanPackages.get(this.context).getPackageNames();
//		if (packages.isEmpty() && AutoConfigurationPackages.has(this.context)) {
//			packages = AutoConfigurationPackages.get(this.context);
//		}
//		return packages;
	}

}
