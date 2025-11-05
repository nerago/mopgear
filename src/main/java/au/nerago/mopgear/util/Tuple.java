package au.nerago.mopgear.util;

import java.util.Objects;

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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Tuple1<?> tuple1 = (Tuple1<?>) o;
            return Objects.equals(a, tuple1.a);
        }

        @Override
        public int hashCode() {
            return Objects.hash(a);
        }

        @Override
        public String toString() {
            return "Tuple{" + a + '}';
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Tuple2<?, ?> tuple2 = (Tuple2<?, ?>) o;
            return Objects.equals(a, tuple2.a) && Objects.equals(b, tuple2.b);
        }

        @Override
        public int hashCode() {
            return Objects.hash(a, b);
        }

        @Override
        public String toString() {
            return "Tuple{" + a +
                    ", " + b +
                    '}';
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Tuple3<?, ?, ?> tuple3 = (Tuple3<?, ?, ?>) o;
            return Objects.equals(a, tuple3.a) && Objects.equals(b, tuple3.b) && Objects.equals(c, tuple3.c);
        }

        @Override
        public int hashCode() {
            return Objects.hash(a, b, c);
        }

        @Override
        public String toString() {
            return "Tuple{" + a +
                    ", " + b +
                    ", " + c +
                    '}';
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

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Tuple4<?, ?, ?, ?> tuple4 = (Tuple4<?, ?, ?, ?>) o;
            return Objects.equals(a, tuple4.a) && Objects.equals(b, tuple4.b) && Objects.equals(c, tuple4.c) && Objects.equals(d, tuple4.d);
        }

        @Override
        public int hashCode() {
            return Objects.hash(a, b, c, d);
        }

        @Override
        public String toString() {
            return "Tuple{" + a +
                    ", " + b +
                    ", " + c +
                    ", " + d +
                    '}';
        }
    }
}
