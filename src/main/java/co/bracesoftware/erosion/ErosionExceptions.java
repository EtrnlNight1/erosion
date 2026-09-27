package co.bracesoftware.erosion;

public class ErosionExceptions
{
    public static abstract sealed class ErosionException extends RuntimeException
    {
        public ErosionException(String e)
        {
            super(e);
        }
    }

    public static final class ErosionMixinException extends ErosionException
    {
        public ErosionMixinException(String e)
        {
            super(e);
        }
    }

    public static final class ErosionRecipeImplException extends ErosionException
    {
        public ErosionRecipeImplException(String e)
        {
            super(e);
        }
    }

    public static final class ErosionDataGenException extends ErosionException
    {
        public ErosionDataGenException(String e)
        {
            super(e);
        }
    }

    public static final class ErosionEventBusException extends ErosionException
    {
        public ErosionEventBusException(String e)
        {
            super(e);
        }
    }

    public static final class ErosionBlockEntityExceptions
    {
        public static final class ErosionMaterialPurifierException extends ErosionException
        {
            public ErosionMaterialPurifierException(String e)
            {
                super(e);
            }
        }
        public static final class ErosionCrucibleException extends ErosionException
        {
            public ErosionCrucibleException(String e)
            {
                super(e);
            }
        }
    }

    public static final class ErosionAPIExceptions
    {
        public static final class ErosionDisplayMessageException extends ErosionException
        {
            public ErosionDisplayMessageException(String e)
            {
                super(e);
            }
        }
    }

    public static final class ErosionCommandExceptions
    {
        public static final class ErosionCommandParserException extends ErosionException
        {
            public ErosionCommandParserException(String e)
            {
                super(e);
            }
        }
        public static final class ErosionCommandSetupException extends ErosionException
        {
            public ErosionCommandSetupException(String e)
            {
                super(e);
            }
        }
    }

    public static final class ErosionCustomEntityExceptions
    {
        public static final class ErosionGasInitException extends ErosionException
        {
            public ErosionGasInitException(String e)
            {
                super(e);
            }
        }
    }
    public static class ErosionBlockExceptions
    {
        public static final class ErosionBlockWithTipImpl extends ErosionException
        {
            public ErosionBlockWithTipImpl(String e)
            {
                super(e);
            }
        }

        public static final class ErosionNetworkSafeBlockException extends ErosionException
        {
            public ErosionNetworkSafeBlockException(String e)
            {
                super(e);
            }
        }

        public static final class ErosionChemicalReactorException extends ErosionException
        {
            public ErosionChemicalReactorException(String e)
            {
                super(e);
            }
        }
    }

    public static final class ErosionItemExceptions
    {
        public static final class ErosionGasMaskInitException extends ErosionException
        {
            public ErosionGasMaskInitException(String e)
            {
                super(e);
            }
        }
    }

    public static final class ErosionConfigException
    {
        public static final class ErosionWrongConfigGetterOrSetterMethodCalledException extends ErosionException
        {
            public ErosionWrongConfigGetterOrSetterMethodCalledException(String e)
            {
                super(e);
            }
        }
    }
}