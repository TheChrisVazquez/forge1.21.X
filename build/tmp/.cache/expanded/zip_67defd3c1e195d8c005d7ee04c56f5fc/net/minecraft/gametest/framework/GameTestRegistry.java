package net.minecraft.gametest.framework;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Rotation;

public class GameTestRegistry {
    private static final Collection<TestFunction> TEST_FUNCTIONS = Lists.newArrayList();
    private static final Set<String> TEST_CLASS_NAMES = Sets.newHashSet();
    private static final Map<String, Consumer<ServerLevel>> BEFORE_BATCH_FUNCTIONS = Maps.newHashMap();
    private static final Map<String, Consumer<ServerLevel>> AFTER_BATCH_FUNCTIONS = Maps.newHashMap();
    private static final Set<TestFunction> LAST_FAILED_TESTS = Sets.newHashSet();


    /** @deprecated Forge: Use {@link net.minecraftforge.event.RegisterGameTestsEvent RegisterGameTestsEvent} to register game tests */
    @Deprecated
    public static void register(Class<?> pTestClass) {
        Arrays.stream(pTestClass.getDeclaredMethods()).sorted(Comparator.comparing(Method::getName)).forEach(GameTestRegistry::register);
    }

    /** @deprecated Forge: Use {@link net.minecraftforge.event.RegisterGameTestsEvent RegisterGameTestsEvent} to register game tests */
    @Deprecated
    public static void register(Method p_177504_) {
        register(p_177504_, java.util.Set.of());
    }

    /** @deprecated Forge: Use {@link net.minecraftforge.event.RegisterGameTestsEvent RegisterGameTestsEvent} to register game tests */
    @Deprecated
    public static void register(Method p_177504_, java.util.Set<String> allowedNamespaces) {
        String s = p_177504_.getDeclaringClass().getSimpleName();
        GameTest gametest = p_177504_.getAnnotation(GameTest.class);
        if (gametest != null) {
            net.minecraftforge.gametest.ForgeGameTestHooks.addTest(TEST_FUNCTIONS, TEST_CLASS_NAMES, allowedNamespaces, turnMethodIntoTestFunction(p_177504_));
        }

        GameTestGenerator gametestgenerator = p_177504_.getAnnotation(GameTestGenerator.class);
        if (gametestgenerator != null) {
            for (var func : useTestGeneratorMethod(p_177504_)) {
                net.minecraftforge.gametest.ForgeGameTestHooks.addTest(TEST_FUNCTIONS, TEST_CLASS_NAMES, allowedNamespaces, func);
            }
        }

        registerBatchFunction(p_177504_, BeforeBatch.class, BeforeBatch::batch, BEFORE_BATCH_FUNCTIONS);
        registerBatchFunction(p_177504_, AfterBatch.class, AfterBatch::batch, AFTER_BATCH_FUNCTIONS);
    }

    private static <T extends Annotation> void registerBatchFunction(
        Method pTestMethod, Class<T> pAnnotationType, Function<T, String> pValueGetter, Map<String, Consumer<ServerLevel>> pPositioning
    ) {
        T t = pTestMethod.getAnnotation(pAnnotationType);
        if (t != null) {
            String s = pValueGetter.apply(t);
            Consumer<ServerLevel> consumer = pPositioning.putIfAbsent(s, (Consumer<ServerLevel>)turnMethodIntoConsumer(pTestMethod));
            if (consumer != null) {
                throw new RuntimeException("Hey, there should only be one " + pAnnotationType + " method per batch. Batch '" + s + "' has more than one!");
            }
        }
    }

    public static Stream<TestFunction> getTestFunctionsForClassName(String pClassName) {
        return TEST_FUNCTIONS.stream().filter(p_127674_ -> isTestFunctionPartOfClass(p_127674_, pClassName));
    }

    public static Collection<TestFunction> getAllTestFunctions() {
        return TEST_FUNCTIONS;
    }

    public static Collection<String> getAllTestClassNames() {
        return TEST_CLASS_NAMES;
    }

    public static boolean isTestClass(String pClassName) {
        return TEST_CLASS_NAMES.contains(pClassName);
    }

    public static Consumer<ServerLevel> getBeforeBatchFunction(String pFunctionName) {
        return BEFORE_BATCH_FUNCTIONS.getOrDefault(pFunctionName, p_325944_ -> {
        });
    }

    public static Consumer<ServerLevel> getAfterBatchFunction(String pFunctionName) {
        return AFTER_BATCH_FUNCTIONS.getOrDefault(pFunctionName, p_325941_ -> {
        });
    }

    public static Optional<TestFunction> findTestFunction(String pTestName) {
        return getAllTestFunctions().stream().filter(p_325943_ -> p_325943_.testName().equalsIgnoreCase(pTestName)).findFirst();
    }

    public static TestFunction getTestFunction(String pTestName) {
        Optional<TestFunction> optional = findTestFunction(pTestName);
        if (optional.isEmpty()) {
            throw new IllegalArgumentException("Can't find the test function for " + pTestName);
        } else {
            return optional.get();
        }
    }

    private static Collection<TestFunction> useTestGeneratorMethod(Method pTestMethod) {
        try {
            Object object = null;
            if (!java.lang.reflect.Modifier.isStatic(pTestMethod.getModifiers())) {
                object = pTestMethod.getDeclaringClass().newInstance();
            }
            return (Collection<TestFunction>)pTestMethod.invoke(object);
        } catch (ReflectiveOperationException reflectiveoperationexception) {
            throw new RuntimeException(reflectiveoperationexception);
        }
    }

    private static TestFunction turnMethodIntoTestFunction(Method pTestMethod) {
        GameTest gametest = pTestMethod.getAnnotation(GameTest.class);
        String s = pTestMethod.getDeclaringClass().getSimpleName();
        String s1 = s.toLowerCase();
        String s2 = net.minecraftforge.gametest.ForgeGameTestHooks.getTestName(pTestMethod, gametest);
        String s3 = net.minecraftforge.gametest.ForgeGameTestHooks.getTestTemplate(pTestMethod, gametest, s2);
        String s4 = net.minecraftforge.gametest.ForgeGameTestHooks.getTestBatch(pTestMethod, gametest);
        Rotation rotation = StructureUtils.getRotationForRotationSteps(gametest.rotationSteps());
        return new TestFunction(
            s4,
            s2,
            s3,
            rotation,
            gametest.timeoutTicks(),
            gametest.setupTicks(),
            gametest.required(),
            gametest.manualOnly(),
            gametest.requiredSuccesses(),
            gametest.attempts(),
            gametest.skyAccess(),
            (Consumer<GameTestHelper>)turnMethodIntoConsumer(pTestMethod)
        );
    }

    private static Consumer<?> turnMethodIntoConsumer(Method pTestMethod) {
        return p_177512_ -> {
            try {
                Object object = null;
                if (!java.lang.reflect.Modifier.isStatic(pTestMethod.getModifiers())) {
                    object = pTestMethod.getDeclaringClass().newInstance();
                }
                pTestMethod.invoke(object, p_177512_);
            } catch (InvocationTargetException invocationtargetexception) {
                if (invocationtargetexception.getCause() instanceof RuntimeException) {
                    throw (RuntimeException)invocationtargetexception.getCause();
                } else {
                    throw new RuntimeException(invocationtargetexception.getCause());
                }
            } catch (ReflectiveOperationException reflectiveoperationexception) {
                throw new RuntimeException(reflectiveoperationexception);
            }
        };
    }

    private static boolean isTestFunctionPartOfClass(TestFunction pTestFunction, String pClassName) {
        return pTestFunction.testName().toLowerCase().startsWith(pClassName.toLowerCase() + ".");
    }

    public static Stream<TestFunction> getLastFailedTests() {
        return LAST_FAILED_TESTS.stream();
    }

    public static void rememberFailedTest(TestFunction pTestFunction) {
        LAST_FAILED_TESTS.add(pTestFunction);
    }

    public static void forgetFailedTests() {
        LAST_FAILED_TESTS.clear();
    }
}
