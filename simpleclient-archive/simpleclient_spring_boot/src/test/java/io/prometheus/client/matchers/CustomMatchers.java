package io.prometheus.client.matchers;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.core.IsCollectionContaining;

/**
 * @author <a href="http://stackoverflow.com/users/4483548/bretc">BretC</a>
 *
 * @see <a href="http://stackoverflow.com/a/29610402/346545">this StackOverflow answer</a>
 *
 * Licensed under Creative Commons BY-SA 3.0
 */
public final class CustomMatchers {

  private CustomMatchers() {
  }

  public static <T> Matcher<Iterable<? super T>> exactlyNItems(final int n, final Matcher<? super T> elementMatcher) {
    return new IsCollectionContaining<T>(elementMatcher) {
      @Override
      protected boolean matchesSafely(Iterable<? super T> collection, Description mismatchDescription) {
        int count = 0;
        boolean isPastFirst = false;

        for (Object item : collection) {

          if (elementMatcher.matches(item)) {
            count++;
          }
          if (isPastFirst) {
            mismatchDescription.appendText(", ");
          }
          elementMatcher.describeMismatch(item, mismatchDescription);
          isPastFirst = true;
        }

        if (count != n) {
          mismatchDescription.appendText(". Expected exactly " + n + " but got " + count);
        }
        return count == n;
      }
    };
  }
}
