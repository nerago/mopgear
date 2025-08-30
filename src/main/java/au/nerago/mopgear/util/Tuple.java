package au.nerago.mopgear.util;

public interface Tuple {
    Object x(int x);

    static <A> Tuple1<A> create(A a) {
        return new Tuple1<>(a);
    }

    static <A, B> Tuple2<A, B> create(A a, B b) {
        return new Tuple2<>(a, b);
    }

    static <A, B, C> Tuple3<A, B, C> create(A a, B b, C c) {
        return new Tuple3<>(a, b, c);
    }

    static <A, B, C, D> Tuple4<A, B, C, D> create(A a, B b, C c, D d) {
        return new Tuple4<>(a, b, c, d);
    }

    record Tuple1<A>(A a) implements Tuple {
        @SuppressWarnings("SwitchStatementWithTooFewBranches")
        @Override
        public Object x(int x) {
            return switch (x) {
                case 0 -> a;
                default -> throw new IndexOutOfBoundsException();
            };
        }
    }

    record Tuple2<A, B>(A a, B b) implements Tuple {
        @Override
        public Object x(int x) {
            return switch (x) {
                case 0 -> a;
                case 1 -> b;
                default -> throw new IndexOutOfBoundsException();
            };
        }
    }

    record Tuple3<A, B, C>(A a, B b, C c) implements Tuple {
        @Override
        public Object x(int x) {
            return switch (x) {
                case 0 -> a;
                case 1 -> b;
                case 2 -> c;
                default -> throw new IndexOutOfBoundsException();
            };
        }
    }

    record Tuple4<A, B, C, D>(A a, B b, C c, D d) implements Tuple {
        @Override
        public Object x(int x) {
            return switch (x) {
                case 0 -> a;
                case 1 -> b;
                case 2 -> c;
                case 3 -> d;
                default -> throw new IndexOutOfBoundsException();
            };
        }
    }
}
