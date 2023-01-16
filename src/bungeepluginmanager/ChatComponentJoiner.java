package bungeepluginmanager;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.Objects;
import java.util.stream.Collector;

public class ChatComponentJoiner {
	private BaseComponent prefix;
	private BaseComponent delimiter;
	private BaseComponent suffix;

	/*
	 * ComponentBuilder value -- at any time, the characters constructed from the
	 * prefix, the added element separated by the delimiter, but without the
	 * suffix, so that we can more easily add elements without having to jigger
	 * the suffix each time.
	 */
	private ComponentBuilder value;

	/*
	 * By default, the array consisting of {prefix, suffix}, returned by
	 * create(), or properties of value, when no elements have yet been added,
	 * i.e. when it is empty. This may be overridden by the user to be some
	 * other value including an empty array.
	 */
	private BaseComponent[] emptyValue;


	/**
	 * Constructs a {@code ChatComponentJoiner} with no characters in it, with no
	 * {@code prefix} or {@code suffix}, and a copy of the supplied
	 * {@code delimiter}.
	 * If no characters are added to the {@code ChatComponentJoiner} and methods
	 * accessing the value of it are invoked, it will not return a
	 * {@code prefix} or {@code suffix} (or properties thereof) in the result,
	 * unless {@code setEmptyValue} has first been called.
	 *
	 * @param  delimiter the sequence of characters to be used between each
	 *         element added to the {@code ChatComponentJoiner} value
	 * @throws NullPointerException if {@code delimiter} is {@code null}
	 */
	public ChatComponentJoiner(BaseComponent delimiter) {
		this(delimiter, null,  null);
	}

	/**
	 * Constructs a {@code ChatComponentJoiner} with no characters in it using copies
	 * of the supplied {@code prefix}, {@code delimiter} and {@code suffix}.
	 * If no characters are added to the {@code ChatComponentJoiner} and methods
	 * accessing the string value of it are invoked, it will return the
	 * {@code prefix + suffix} (or properties thereof) in the result, unless
	 * {@code setEmptyValue} has first been called.
	 *
	 * @param  delimiter the sequence of characters to be used between each
	 *         element added to the {@code ChatComponentJoiner}
	 * @param  prefix the sequence of characters to be used at the beginning
	 * @param  suffix the sequence of characters to be used at the end
	 * @throws NullPointerException if {@code prefix}, {@code delimiter}, or
	 *         {@code suffix} is {@code null}
	 */
	public ChatComponentJoiner(BaseComponent delimiter,
							   BaseComponent prefix,
							   BaseComponent suffix) {
		Objects.requireNonNull(delimiter, "The delimiter must not be null");

		if (prefix != null) {
			this.prefix = prefix.duplicate();
		}

		this.delimiter = delimiter.duplicate();

		if (suffix != null) {
			this.suffix = suffix.duplicate();
		}

		this.emptyValue = new BaseComponent[]{};

		if (this.prefix != null) {
			this.emptyValue = new BaseComponent[] {this.prefix.duplicate()};
		}

		if (this.suffix != null) {
			if (this.emptyValue.length > 0) {
				this.emptyValue = new BaseComponent[] {this.emptyValue[0], this.suffix.duplicate()};
			}
			else {
				this.emptyValue = new BaseComponent[] {this.suffix.duplicate()};
			}
		}
	}

	/**
	 * Sets the sequence of characters to be used when determining the string
	 * representation of this {@code ChatComponentJoiner} and no elements have been
	 * added yet, that is, when it is empty.  A copy of the {@code emptyValue}
	 * parameter is made for this purpose. Note that once an add method has been
	 * called, the {@code ChatComponentJoiner} is no longer considered empty, even if
	 * the element(s) added correspond to the empty {@code String}.
	 *
	 * @param  emptyValue the characters to return as the value of an empty
	 *         {@code ChatComponentJoiner}
	 * @return this {@code ChatComponentJoiner} itself so the calls may be chained
	 * @throws NullPointerException when the {@code emptyValue} parameter is
	 *         {@code null}
	 */
	public ChatComponentJoiner setEmptyValue(BaseComponent[] emptyValue) {
		this.emptyValue = Objects.requireNonNull(emptyValue,
			"The empty value must not be null");
		return this;
	}

	/**
	 * Adds the contents of the given {@code ChatComponentJoiner} without prefix and
	 * suffix as the next element if it is non-empty. If the given {@code
	 * ChatComponentJoiner} is empty, the call has no effect.
	 *
	 * <p>A {@code ChatComponentJoiner} is empty if {@link #add(BaseComponent) add()}
	 * has never been called, and if {@code merge()} has never been called
	 * with a non-empty {@code ChatComponentJoiner} argument.
	 *
	 * <p>If the other {@code ChatComponentJoiner} is using a different delimiter,
	 * then elements from the other {@code ChatComponentJoiner} are concatenated with
	 * that delimiter and the result is appended to this {@code ChatComponentJoiner}
	 * as a single element.
	 *
	 * @param other The {@code ChatComponentJoiner} whose contents should be merged
	 *              into this one
	 * @throws NullPointerException if the other {@code ChatComponentJoiner} is null
	 * @return This {@code ChatComponentJoiner}
	 */
	public ChatComponentJoiner merge(ChatComponentJoiner other) {
		Objects.requireNonNull(other);
		if (other == this) {
			throw new IllegalArgumentException();
		}
		if (other.value != null) {
			ComponentBuilder builder = prepareBuilder();

			// We start at 1 because we want to skip the prefix
			for (int i = 1; i < other.value.getParts().size(); i++) {
				builder.append(other.value.getParts().get(i));
			}
		}
		return this;
	}

	/**
	 * Returns the current value, consisting of the {@code prefix}, the values
	 * added so far separated by the {@code delimiter}, and the {@code suffix},
	 * unless no elements have been added in which case, the
	 * {@code prefix + suffix} or the {@code emptyValue} characters are returned
	 *
	 * @return the current value
	 */
	public BaseComponent[] create() {
		if (this.value == null) {
			return this.emptyValue;
		}
		else if (this.suffix == null) {
			return this.value.create();
		}
		else {

			// Append suffix then remove it
			this.value.append(this.suffix.duplicate());
			BaseComponent[] result = this.value.create();
			this.value.removeComponent(this.value.getCursor());
			return result;
		}
	}

	/**
	 * Adds a copy of the given {@code BaseComponent} value as the next
	 * element of the {@code ChatComponentJoiner} value. If {@code newElement} is
	 * {@code null}, then {@code "null"} is added.
	 *
	 * @param  newElement The element to add
	 * @return a reference to this {@code ChatComponentJoiner}
	 */
	public ChatComponentJoiner add(BaseComponent newElement) {
		prepareBuilder().append(newElement);
		return this;
	}

	private ComponentBuilder prepareBuilder() {
		if (value != null) {
			value.append(delimiter.duplicate());
		} else {
			this.value = new ComponentBuilder();

			if (this.prefix != null) {
				this.value.append(this.prefix.duplicate());
			}
		}
		return value;
	}


	/**
	 * Returns a {@code Collector} that concatenates the input elements into a
	 * {@code BaseComponent[]}, in encounter order.
	 */
	public static Collector<BaseComponent, ?, BaseComponent[]> joining() {
		return Collector.of(
			ComponentBuilder::new, ComponentBuilder::append,
			(r1, r2) -> {
				for (int i = 0; i < r2.getParts().size(); i++) {
					r1.append(r2.getParts().get(i));
				}
				return r1;
			},
			ComponentBuilder::create);
	}

	/**
	 * Returns a {@code Collector} that concatenates the input elements,
	 * separated by the specified delimiter, in encounter order.
	 *
	 * @param delimiter the delimiter to be used between each element
	 * @return A {@code Collector} which concatenates CharSequence elements,
	 * separated by the specified delimiter, in encounter order
	 */
	public static Collector<BaseComponent, ?, BaseComponent[]> joining(String delimiter) {
		return joining(new TextComponent(delimiter));
	}

	/**
	 * Returns a {@code Collector} that concatenates the input elements,
	 * separated by the specified delimiter, with the specified prefix and
	 * suffix, in encounter order.
	 *
	 * @param delimiter the delimiter to be used between each element
	 * @param  prefix the sequence of characters to be used at the beginning
	 *                of the joined result
	 * @param  suffix the sequence of characters to be used at the end
	 *                of the joined result
	 * @return A {@code Collector} which concatenates CharSequence elements,
	 * separated by the specified delimiter, in encounter order
	 */
	public static Collector<BaseComponent, ?, BaseComponent[]> joining(String delimiter, String prefix, String suffix) {
		return joining(new TextComponent(delimiter), prefix != null ? new TextComponent(prefix) : null, suffix != null ? new TextComponent(suffix) : null);
	}

	/**
	 * Returns a {@code Collector} that concatenates the input elements,
	 * separated by the specified delimiter, in encounter order.
	 *
	 * @param delimiter the delimiter to be used between each element
	 * @return A {@code Collector} which concatenates CharSequence elements,
	 * separated by the specified delimiter, in encounter order
	 */
	public static Collector<BaseComponent, ?, BaseComponent[]> joining(BaseComponent delimiter) {
		return joining(delimiter, null, null);
	}

	/**
	 * Returns a {@code Collector} that concatenates the input elements,
	 * separated by the specified delimiter, with the specified prefix and
	 * suffix, in encounter order.
	 *
	 * @param delimiter the delimiter to be used between each element
	 * @param  prefix the sequence of characters to be used at the beginning
	 *                of the joined result
	 * @param  suffix the sequence of characters to be used at the end
	 *                of the joined result
	 * @return A {@code Collector} which concatenates CharSequence elements,
	 * separated by the specified delimiter, in encounter order
	 */
	public static Collector<BaseComponent, ChatComponentJoiner, BaseComponent[]> joining(BaseComponent delimiter, BaseComponent prefix, BaseComponent suffix) {
		return Collector.of(() -> new ChatComponentJoiner(delimiter, prefix, suffix),
			ChatComponentJoiner::add,
			ChatComponentJoiner::merge,
			ChatComponentJoiner::create);
	}
}