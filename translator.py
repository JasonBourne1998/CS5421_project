from tkinter import RIGHT
from turtle import right


class Node:
    def getAttributes():
        pass
    def setAttributes(attributes):
        pass

class UnaryOperator(Node):
    def __init__(self, operator, attributes, child_operand = None):
        self.operator = operator
        self.attributes = attributes
        self.child_operand = child_operand
        self.relations = child_operand.relations if child_operand else [attributes]

class BinaryOperator(Node):
    def __init__(self, operator, attributes, left_operand, right_operand):
        self.operator = operator
        self.attributes = attributes
        self.left_operand = left_operand
        self.right_operand = right_operand
        self.relations = left_operand.relations + right_operand.relations

class Gamma(UnaryOperator):
    def __init__(self, operator, attributes, aggregate, target_attr, child_operand = None):
        self.operator = operator
        self.attributes = attributes
        self.child_operand = child_operand
        self.relations = child_operand.relations if child_operand else [attributes]
        self.aggregate = aggregate
        self.target_attr = target_attr




class OutputSQL:
    first = True
    SELECT = []
    FROM = []
    WHERE = []
    ORDERBY = []
    GROUPBY = []
    
    @classmethod
    def output(self):
        if len(OutputSQL.SELECT)==0:
            OutputSQL.SELECT.append('*')
        print('SELECT '+', '.join(OutputSQL.SELECT))
        print('FROM '+', '.join(OutputSQL.FROM))
        if len(OutputSQL.WHERE)>0:
            print('WHERE '+' AND '.join(OutputSQL.WHERE))
        if len(OutputSQL.GROUPBY)>0:
            print('GROUP BY ' +', '.join(OutputSQL.GROUPBY))
        if len(OutputSQL.ORDERBY)>0:
            print('ORDER BY '+', '.join(OutputSQL.ORDERBY))

def main():
    n1 = UnaryOperator('ro','downloads d')
    n2 = UnaryOperator('ro','customers c')
    n3 = BinaryOperator('natural join','d.customerid=c.customerid',n1, n2)
    n4 = UnaryOperator('ro','games g')
    n5 = BinaryOperator('natural join','d.game=g.name∧d.version=g.version',n3, n4)
    n6 = UnaryOperator('sigma','c.country=\'Singapore\'', n5)
    n7 = UnaryOperator('pi','g.name', n6)
    n8 = UnaryOperator('tao','g.name',n7)
    n9 = Gamma('gamma','g.name','count','age', n8)
    traverse(n9)
    OutputSQL.output()

def traverse(root):
    if root == None:
        return
    if isinstance(root, UnaryOperator):
        if root.operator == 'pi' and OutputSQL.first:
            OutputSQL.first = False
            OutputSQL.SELECT.append(root.attributes)
        elif root.operator == 'sigma':
            OutputSQL.WHERE.append(root.attributes)
        elif root.operator == 'tao':
            OutputSQL.ORDERBY.append(root.attributes)
        elif root.operator == 'gamma':
            OutputSQL.GROUPBY.append(root.attributes)
            OutputSQL.SELECT.append(root.aggregate+'('+root.target_attr+')')
            
        traverse(root.child_operand)
    if isinstance(root, BinaryOperator):
        if root.operator == 'natural join':
            OutputSQL.WHERE.append(root.attributes)
            if root.left_operand.relations not in OutputSQL.FROM:
                for r in root.left_operand.relations:
                    if r not in OutputSQL.FROM:
                        OutputSQL.FROM.append(r)
            if root.right_operand.relations not in OutputSQL.FROM:
                for r in root.right_operand.relations:
                    if r not in OutputSQL.FROM:
                        OutputSQL.FROM.append(r)
        elif root.operator == 'union':
            pass
        elif root.operator == 'cartesian':
            OutputSQL.FROM.append(root.left_operand.relations)
            OutputSQL.FROM.append(root.right_operand.relations)
        
if __name__ == "__main__":
    main()
