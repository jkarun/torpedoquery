package org.torpedoquery.jpa.internal;

import java.util.Deque;
import java.util.Map;

import org.torpedoquery.jpa.Function;
import org.torpedoquery.jpa.OnGoingCondition;

//TODO duplicate avec WhereClauseCollectionHandler
public class WhereClauseHandler<T, E extends OnGoingCondition<T>> implements QueryHandler<E> {

	private final LogicalCondition logicalCondition;
	private final Function<T> function;
	private final QueryConfigurator<T> configurator;

	public WhereClauseHandler() {
		this(null, new WhereQueryConfigurator<T>());
	}

	public WhereClauseHandler(QueryConfigurator<T> configurator) {
		this(null, configurator);
	}

	public WhereClauseHandler(LogicalCondition logicalCondition, QueryConfigurator<T> configurator) {
		this(null, logicalCondition, configurator);
	}

	public WhereClauseHandler(Function<T> function, LogicalCondition logicalCondition, QueryConfigurator<T> configurator) {
		this.function = function;
		this.logicalCondition = logicalCondition;
		this.configurator = configurator;
	}

	@Override
	public E handleCall(Map<Object, QueryBuilder<?>> proxyQueryBuilders, Deque<MethodCall> methodCalls) {

		Selector<T> conditionSelector = function;

		final QueryBuilder<T> queryImpl;

		if (conditionSelector == null) {
			MethodCall pollFirst = methodCalls.pollFirst();
			queryImpl = (QueryBuilder<T>) proxyQueryBuilders.get(pollFirst.getProxy());
			conditionSelector = new SimpleMethodCallSelector<T>(queryImpl, pollFirst);
		} else {
			queryImpl = (QueryBuilder<T>) proxyQueryBuilders.get(function.getProxy());
		}

		final ConditionBuilder<T> whereClause = logicalCondition != null ? new ConditionBuilder<T>(queryImpl, logicalCondition, conditionSelector)
				: new ConditionBuilder<T>(queryImpl, conditionSelector);

		configurator.configure(queryImpl, whereClause);

		return (E) whereClause;
	}
}