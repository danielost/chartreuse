package chartreuse

import doodle.algebra.Algebra
import doodle.algebra.Picture
import doodle.algebra.Shape
import doodle.core.Color
import doodle.language.Basic
import doodle.syntax.all.*

/** Glyph describes how to turn a data point into a graphical mark (a "glyph")
  * in the plot.
  *
  * The data point has type `A` and the glyph requires algebra's `Alg` to draw.
  *
  * As this is a trait, it is open for extension. However, see the `Glyph`
  * companion object for commonly used cases. Note that `Glyph` instances should
  * not position the glyph. That is, they should not call `at` or other methods
  * that change the position of the `Picture` origin or bounding box unless that
  * is essential to correctly producing the glyph.
  */
trait Glyph[-A, Alg <: Algebra] {

  /** Given a data point, turn it into a glyph */
  def render(data: A): Picture[Alg, Unit]
}
object Glyph {

  /** Little algebra for constructing Glyph instances that supports the common
    * operations.
    */
  enum Simple[A] extends Glyph[A, Basic] {
    case Contramap[A, B](source: Simple[A], f: B => A) extends Simple[B]
    case Shape(glyph: A => Picture[Basic, Unit])
    case Style(
        source: Simple[A],
        style: Picture[Basic, Unit] => Picture[Basic, Unit]
    )

    def contramap[B](f: B => A): Simple[B] =
      Contramap(this, f)

    def render(data: A): Picture[Basic, Unit] =
      this match {
        case Contramap(source, f) => source.render(f(data))
        case Shape(glyph)         => glyph(data)
        case Style(source, style) => style(source.render(data))
      }

    def fillColor(color: Color): Simple[A] =
      Style(this, picture => picture.fillColor(color))

    def noFill: Simple[A] =
      Style(this, picture => picture.noFill)

    def strokeColor(color: Color): Simple[A] =
      Style(this, picture => picture.strokeColor(color))

    def noStroke: Simple[A] =
      Style(this, picture => picture.noStroke)
  }

  val circle: Simple[Double] =
    Simple.Shape(radius => doodle.syntax.shape.circle[Basic](radius * 2))
}
