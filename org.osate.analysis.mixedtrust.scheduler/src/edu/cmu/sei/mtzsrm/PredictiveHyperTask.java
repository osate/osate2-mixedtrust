/**
 * Mixed-Trust Scheduling Analysis OSATE Plugin
 *
 * Copyright 2021 Carnegie Mellon University.
 *
 * NO WARRANTY. THIS CARNEGIE MELLON UNIVERSITY AND SOFTWARE ENGINEERING
 * INSTITUTE MATERIAL IS FURNISHED ON AN "AS-IS" BASIS. CARNEGIE MELLON
 * UNIVERSITY MAKES NO WARRANTIES OF ANY KIND, EITHER EXPRESSED OR IMPLIED,
 * AS TO ANY MATTER INCLUDING, BUT NOT LIMITED TO, WARRANTY OF FITNESS FOR
 * PURPOSE OR MERCHANTABILITY, EXCLUSIVITY, OR RESULTS OBTAINED FROM USE OF
 * THE MATERIAL. CARNEGIE MELLON UNIVERSITY DOES NOT MAKE ANY WARRANTY OF
 * ANY KIND WITH RESPECT TO FREEDOM FROM PATENT, TRADEMARK, OR COPYRIGHT
 * INFRINGEMENT.
 *
 * Released under the Eclipse Public License - v 2.0 license, please see
 * license.txt or contact permission@sei.cmu.edu for full terms.
 *
 * [DISTRIBUTION STATEMENT A] This material has been approved for public
 * release and unlimited distribution.  Please see Copyright notice for
 * non-US Government use and distribution.
 *
 * Carnegie Mellon® is registered in the U.S. Patent and Trademark Office
 * by Carnegie Mellon University.
 *
 * DM21-0927
 */

package edu.cmu.sei.mtzsrm;

public class PredictiveHyperTask extends HyperTask {
	int predictiveExecTime;

	public void setPredictiveExecTime(int p) {
		predictiveExecTime = p;
	}

	public int getPredictiveExecTime() {
		return predictiveExecTime;
	}

	public int getMaxExecTime() {
		return (predictiveExecTime > exectime)? predictiveExecTime : exectime;
	}

	int framePeriods;

	public void setFramePeriods(int f) {
		framePeriods = f;
	}

	public int getFramePeriods() {
		return framePeriods;
	}

	public PredictiveHyperTask(int criticality, int exectime, int pet, int framePeriods)
	{
		super(criticality,exectime);
		this.predictiveExecTime = pet;
		this.framePeriods = framePeriods;
	}

	public PredictiveHyperTask() {
		super();
	}
}
