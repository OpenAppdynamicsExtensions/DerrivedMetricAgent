package com.appdynamics.ace.agents.derivedMetrics.groovy.cli.api

import com.appdynamics.ace.util.cli.api.api.AbstractCommand
import com.appdynamics.ace.util.cli.api.api.CommandException
import com.appdynamics.ace.util.cli.api.api.OptionWrapper
import org.apache.commons.cli.Option

/**
 * Created by stefan.marx on 25.09.15.
 */
class CommandWrapper extends AbstractCommand{
    private String _name
    private String _desc
    private Map<String, Map> _opts
    private Closure _call

    CommandWrapper(String name, String desc, Map<String, Map> opts, Closure call) {

        this._call = call
        this._opts = opts
        this._desc = desc
        this._name = name
    }

    @Override
    protected List<Option> getCLIOptionsImpl() {
        def o = []
        _opts.each {k,v ->
            Option tmp = new Option(k,
            v['args']?:false,
            v['desc']?:k);

            tmp.setRequired(!(v['opt']?:false));
            o+=tmp
        }
        return o
    }

    @Override
    protected int executeImpl(OptionWrapper optionWrapper) throws CommandException {

        def values = [:]
        _opts.each {k,v ->
            if (optionWrapper.hasOption(k)) {
                List v2 = optionWrapper.getOptionValues(k);
                if (v['args']) values[k] = (v2.size()>1)?v2:v2.first()
                else values[k] = true;
            } else {
                if ((v['opt']?:false)) {
                    if (v.containsKey('def')) {
                        values[k]=v['def'];
                    }
                }
            }
        }

        switch(_call.maximumNumberOfParameters) {
            case 0: return _call.call()?:0
            case 1: return _call.call(values)?:0
            case 2: return _call.call(values,optionWrapper.getArgs())?:0

            default: throw new CommandException("Wrong number of Parameters expected in Closure : "+_call.maximumNumberOfParameters)
        }


    }

    @Override
    String getName() {
        return _name
    }

    @Override
    String getDescription() {
        return _desc
    }
}
