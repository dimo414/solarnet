package dev.martianzoo.tfm.petaform.parser

import com.google.common.truth.Truth.assertThat
import dev.martianzoo.tfm.petaform.api.Expression
import dev.martianzoo.tfm.petaform.api.This
import org.junit.jupiter.api.Test

class ExpressionTest {
  private fun roundTripCte(s: String) =
      assertThat(BetterParser().parseExpression(s).asSource).isEqualTo(s)

  @Test
  fun simpleSourceToApi() {
    val foo = BetterParser().parseExpression("Foo")
    assertThat(foo).isEqualTo(Expression("Foo"))
  }

  @Test
  fun simpleApiToSource() {
    assertThat(Expression("Foo").asSource).isEqualTo("Foo")
  }

  @Test
  fun simpleRoundTrips() {
    roundTripCte("Foo")
    roundTripCte("Foo<Bar>")
    roundTripCte("Foo<Bar, Baz>")
    roundTripCte("Foo<Bar<Qux>, Baz>")
  }

  @Test
  fun complexRoundTrip() {
    roundTripCte("Aa<Bb<Cc<Dd, Ee, Ff<Gg<Hh<Ii>>, Jj>>, Kk>>")
  }

  @Test
  fun complexSourceToApi() {
    val parsed = BetterParser().parseExpression("""
      Red<  // comment works
         Blue  < This,Teal>
        , Gold >
    """)
    assertThat(parsed).isEqualTo(
        Expression("Red",
            Expression("Blue",
                Expression(This),
                Expression("Teal")),
            Expression("Gold")))
  }

  @Test
  fun complexApiToSource() {
    val expr = Expression(
        "Aa",
        Expression("Bb"),
        Expression(
            "Cc",
            Expression("Dd")
        ),
        Expression(
            "Ee",
            Expression(
                This,
                Expression("Gg"),
                Expression("Hh")
            ),
            Expression("Ii")
        ),
        Expression("Jj")
    )
    assertThat(expr.asSource).isEqualTo("Aa<Bb, Cc<Dd>, Ee<This<Gg, Hh>, Ii>, Jj>")
  }
}
