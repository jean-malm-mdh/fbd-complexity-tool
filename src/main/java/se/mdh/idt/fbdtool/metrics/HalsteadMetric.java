package se.mdh.idt.fbdtool.metrics;

import se.mdh.idt.fbdtool.structures.*;

import java.util.HashMap;

/**
 * Created by ado_4 on 3/7/2017.
 */
public class HalsteadMetric implements ComplexityMetric {
  HashMap<String, Double> operators;
  HashMap<String, Double> operands;
  HashMap<String, Double> metric;
  private final double EFFORT_COEFF = 18.0;

  public HalsteadMetric() {
    this.init();
  }

  private void init() {
    this.operators = new HashMap<>();
    this.operands = new HashMap<>();
    this.metric = new HashMap<>();
  }

  private void clear()
  {
    this.operators.clear();
    this.operands.clear();
    this.metric.clear();
  }

  public HashMap<String, Double> getOperators() {
    return operators;
  }

  public HashMap<String, Double> getOperands() {
    return operands;
  }

  private void extractElementOperators(FBDElement el) {
      this.operators.merge(el.getElement(), 1.0, Double::sum);
      this.operators.merge(el.getType(), 1.0, Double::sum);
  }

  private void extractElementOperands(FBDElement el) {
      this.operands.merge(el.getName(), 1.0, Double::sum);
  }

  private void extractVariableOperands(Variable var) {
    this.extractElementOperands(var);
    if (var.getValue() == null) return;

    this.operands.merge(var.getValue(), 1.0, Double::sum);
  }

  private void extractVariableOperators(Variable var) {
    this.extractElementOperators(var);
  }

  private void extractBlockOperators(Block block) {
    if (!block.getType().equals("block")) {
        this.operators.merge(block.getName(), 1.0, Double::sum);
    }
  }

  private void extractBlockOperands(Block block) {
    if (block.getType().equals("block")) {
        this.operands.merge(block.getName(), 1.0, Double::sum);
    }
  }

  private void extractDataTypeOperators(DataType dataType) {
    this.extractElementOperators(dataType);
  }

  private void extractDataTypeOperands(DataType dataType) {
    this.extractElementOperands(dataType);
    if (dataType.getValue() == null)
      return;

    this.operands.merge(dataType.getValue(), 1.0, Double::sum);
  }

  private void extractPOUOperands(POU pou) {
    this.extractElementOperands(pou);
  }

  private void extractPOUOperators(POU pou) {
    this.extractElementOperators(pou);
  }

  private void extractConnectionOperands(int[] connection) {
    String connName = "Connection " + connection[0] + ":" + connection[1];
      this.operands.merge(connName, 1.0, Double::sum);
  }

  private void extractProjectParameters(Project project) {

    for (DataType type : project.getDataTypes()) {
      this.extractDataTypeOperands(type);
      this.extractDataTypeOperators(type);
    }

    for (POU pou : project.getPOUs()) {
      this.extractPOUParameters(pou);
    }
  }

  private void extractPOUParameters(POU pou) {
    this.extractPOUOperands(pou);
    this.extractPOUOperators(pou);

    for (Variable var : pou.getVariables()) {
      this.extractVariableOperands(var);
      this.extractVariableOperators(var);
    }

    for (Block block : pou.getBlocks()) {
      this.extractBlockOperands(block);
      this.extractBlockOperators(block);
    }

    for (int[] conn : pou.getConnections()) {
      this.extractConnectionOperands(conn);
    }
  }

  private double log2(double x) {
    return Math.log(x) / Math.log(2);
  }

  private void calculateHalsteadMetrics() {
    // n1 - unique operators, n2 = unique operands, N1 - total num of operators, N2 - total num of operands
    int n1 = this.operators.size();
    int n2 = this.operands.size();

    double N1 = this.operators.values().stream().mapToDouble(Double::doubleValue).sum();
    double N2 = this.operands.values().stream().mapToDouble(Double::doubleValue).sum();
    double vocabulary = n1 + n2;
    double length = N1 + N2;
    double calculatedLength = n1 * log2(n1) + n2 * log2(n2);
    double volume = length * log2(vocabulary);
    double difficulty = (n1 / 2.0) * (N2 / n2);
    double effort = volume * difficulty;
    double time = effort / EFFORT_COEFF;
    double bugs = Math.pow(effort, 2.0 / 3.0) / 3000.0;

    this.metric.put("UniqueOperators", (double) n1);
    this.metric.put("UniqueOperands", (double) n2);
    this.metric.put("TotalOperators", (double) N1);
    this.metric.put("TotalOperands", (double) N2);
    this.metric.put("ProgramVocabulary", vocabulary);
    this.metric.put("ProgramLength", length);
    this.metric.put("CalculatedProgramLength", calculatedLength);
    this.metric.put("Volume", volume);
    this.metric.put("Difficulty", difficulty);
    this.metric.put("Effort", effort);
    this.metric.put("Time", time);
    this.metric.put("DeliveredBugs", bugs);
  }

  @Override
  public HashMap<String, Double> measureProjectComplexity(Project project) {
    this.clear();
    this.extractProjectParameters(project);
    this.calculateHalsteadMetrics();
    return this.metric;
  }

  @Override
  public HashMap<String, Double> measurePOUComplexity(POU pou) {
    this.clear();
    this.extractPOUParameters(pou);
    this.calculateHalsteadMetrics();
    return this.metric;
  }
}
