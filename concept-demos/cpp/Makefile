# Makefile for Threat Trace C++ Concept Demo
CXX = g++
CXXFLAGS = -std=c++17 -Wall

TARGET = pointer_tree_demo
SRC = pointer_tree_demo.cpp

all: $(TARGET)

$(TARGET): $(SRC)
	$(CXX) $(CXXFLAGS) -o $(TARGET) $(SRC)

clean:
	rm -f $(TARGET) $(TARGET).exe
