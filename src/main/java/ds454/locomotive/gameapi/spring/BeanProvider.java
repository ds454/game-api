package ds454.locomotive.gameapi.spring;

import org.springframework.context.ApplicationContext;

public class BeanProvider {
    private static ApplicationContext context;

    public static void setContext(ApplicationContext ctx) {
        context = ctx;
    }

    public static <T> T getBean(Class<T> repository) {
        if (context == null) {
            throw new IllegalStateException("Spring ApplicationContext has not been initialized yet!");
        }
        return context.getBean(repository);
    }
}
